package com.academiaindustry.service;

import com.academiaindustry.exception.GeminiIntegrationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(OutputCaptureExtension.class)
class GeminiIntegrationServiceTest {

    private static final String API_URL = "https://generativelanguage.googleapis.com";
    private static final String API_KEY = "test-key-that-must-not-be-returned";
    private static final String MODEL = "gemini-2.5-flash";
    private static final String ENDPOINT = API_URL + "/v1beta/models/" + MODEL + ":generateContent";

    private MockRestServiceServer server;
    private GeminiIntegrationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(API_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new GeminiIntegrationService(
                builder.build(), new ObjectMapper(), API_KEY, MODEL);
    }

    @Test
    void sendsPromptToGeminiAndReturnsOnlyGeneratedText() {
        String generatedText = "Which interface preserves insertion order?\nA. HashSet\nB. LinkedHashSet\nC. TreeSet\nD. EnumSet\nAnswer: B";
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Generate one multiple-choice question about Java Collections.")))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"%s"}]}}]}
                        """.formatted(generatedText.replace("\n", "\\n")), MediaType.APPLICATION_JSON));

        String response = service.generateJavaCollectionsQuestion();

        assertEquals(generatedText, response);
        assertFalse(response.contains(API_KEY));
        server.verify();
    }

    @Test
    void sendsAssessmentPromptUsingJsonResponseFormat() {
        String generatedText = "{\"questions\":[]}";
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "\"responseMimeType\":\"application/json\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<requested_topic>Java Collections</requested_topic>")))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"%s"}]}}]}
                        """.formatted(generatedText.replace("\"", "\\\"")), MediaType.APPLICATION_JSON));

        assertEquals(generatedText, service.generateAssessmentQuestions("Java Collections", "Medium", 5));
        server.verify();
    }

    @Test
    void missingApiKeyFailsBeforeMakingRequest() {
        service = new GeminiIntegrationService(RestClient.builder().baseUrl(API_URL).build(),
                new ObjectMapper(), "  ", MODEL);

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals(503, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("GEMINI_API_KEY"));
        server.verify();
    }

    @Test
    void invalidApiKeyReturnsSanitizedError() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(org.springframework.http.HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid key " + API_KEY + "\"}"));

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals(502, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("API returned an error"));
        assertFalse(exception.getMessage().contains(API_KEY));
        server.verify();
    }

    @Test
    void invalidApiKeyReportedAsBadRequestIsRecognized() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"message\":\"API key not valid\"}}"));

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals(502, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("API returned an error"));
        assertFalse(exception.getMessage().contains(API_KEY));
        server.verify();
    }

    @Test
    void upstreamFailuresAreLoggedWithCodesButClientMessageRemainsGeneric(CapturedOutput output) {
        int[] statuses = {404, 429, 503};
        String[] upstreamStatuses = {"NOT_FOUND", "RESOURCE_EXHAUSTED", "UNAVAILABLE"};
        for (int index = 0; index < statuses.length; index++) {
            int status = statuses[index];
            server.expect(requestTo(ENDPOINT))
                    .andRespond(withStatus(org.springframework.http.HttpStatus.valueOf(status))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("""
                                    {"error":{"code":%d,"status":"%s",
                                    "message":"Quota exceeded. api_key=%s; Authorization: Bearer %s; client_secret=%s; refresh_token=%s"}}
                                    """.formatted(status, upstreamStatuses[index], API_KEY,
                                    "authorization-token-secret", "client-secret-value",
                                    "refresh-token-secret")));
        }

        String publicMessage = "Gemini API returned an error. Check the API quota and model configuration.";
        for (int status : statuses) {
            GeminiIntegrationException exception = assertThrows(
                    GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);
            assertEquals(502, exception.getStatus().value());
            assertEquals(publicMessage, exception.getMessage());
        }

        String logs = output.toString();
        for (int status : statuses) {
            assertTrue(logs.contains("httpStatus=" + status));
            assertTrue(logs.contains("geminiCode=" + status));
        }
        assertTrue(logs.contains("geminiStatus=NOT_FOUND"));
        assertTrue(logs.contains("geminiStatus=RESOURCE_EXHAUSTED"));
        assertTrue(logs.contains("geminiStatus=UNAVAILABLE"));
        assertTrue(logs.contains("message=Quota exceeded."));
        assertFalse(logs.contains(API_KEY));
        assertFalse(logs.contains("authorization-token-secret"));
        assertFalse(logs.contains("client-secret-value"));
        assertFalse(logs.contains("refresh-token-secret"));
        server.verify();
    }

    @Test
    void productionErrorDoesNotExposeSanitizedDevelopmentDetails() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"message\":\"Model not found\"}}"));

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals("Gemini API returned an error. Check the API quota and model configuration.",
                exception.getMessage());
        assertFalse(exception.getMessage().contains(MODEL));
        server.verify();
    }

    @Test
    void upstreamFailureReturnsGatewayError() {
        server.expect(requestTo(ENDPOINT)).andRespond(withServerError());

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals(502, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("API returned an error"));
        server.verify();
    }

    @Test
    void timeoutReturnsGatewayTimeout() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(new SocketTimeoutException("read timed out")));

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals(504, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("timeout"));
        server.verify();
    }

    @Test
    void malformedResponseReturnsSanitizedGatewayError() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class, service::generateJavaCollectionsQuestion);

        assertEquals(502, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("malformed"));
        assertFalse(exception.getMessage().contains(API_KEY));
        server.verify();
    }
}
