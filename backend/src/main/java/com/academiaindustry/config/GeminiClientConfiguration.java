package com.academiaindustry.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class GeminiClientConfiguration {

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com";

    @Bean("geminiRestClient")
    public RestClient geminiRestClient(RestClient.Builder builder,
                                      @Value("${app.gemini.connect-timeout:5s}") Duration connectTimeout,
                                      @Value("${app.gemini.read-timeout:30s}") Duration readTimeout) {
        // Keep upstream connection and response waits bounded so diagnostics cannot hang backend requests.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder.clone()
                .baseUrl(GEMINI_API_BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }
}
