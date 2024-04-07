package com.kousenit.claudeai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClaudeRequest(String model,
                            @NotBlank String prompt,
                            int maxTokensToSample,
                            @DecimalMax("2.0") double temperature) {}
