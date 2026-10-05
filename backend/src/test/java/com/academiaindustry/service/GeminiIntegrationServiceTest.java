package com.academiaindustry.service;

import com.academiaindustry.exception.GeminiIntegrationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class GeminiIntegrationServiceTest {

    private static final String API_URL = "https://generativelanguage.googleapis.com";
    private static final String API_KEY = "test-key-that-must-not-be-returned";
    private static final String ENDPOINT = API_URL + "/v1beta/models/gemini-2.5-flash:generateContent";

    private MockRestServiceServer server;
    private GeminiIntegrationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(API_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new GeminiIntegrationService(builder.build(), new ObjectMapper(), API_KEY, "gemini-2.5-flash");
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
    void missingApiKeyFailsBeforeMakingRequest() {
        service = new GeminiIntegrationService(RestClient.builder().baseUrl(API_URL).build(),
                new ObjectMapper(), "  ", "gemini-2.5-flash");

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
        assertTrue(exception.getMessage().contains("rejected the API key"));
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
        assertTrue(exception.getMessage().contains("rejected the API key"));
        assertFalse(exception.getMessage().contains(API_KEY));
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
