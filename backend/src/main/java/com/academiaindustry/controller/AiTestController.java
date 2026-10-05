package com.academiaindustry.controller;

import com.academiaindustry.dto.GeminiTestResponse;
import com.academiaindustry.service.GeminiIntegrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AiTestController {

    private final GeminiIntegrationService geminiIntegrationService;

    public AiTestController(GeminiIntegrationService geminiIntegrationService) {
        this.geminiIntegrationService = geminiIntegrationService;
    }

    @GetMapping("/test")
    public GeminiTestResponse testGeminiConnection() {
        return new GeminiTestResponse(geminiIntegrationService.generateJavaCollectionsQuestion());
    }
}
