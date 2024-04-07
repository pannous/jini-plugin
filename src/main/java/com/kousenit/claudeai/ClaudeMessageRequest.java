package com.kousenit.claudeai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClaudeMessageRequest(
        String model,
        @JsonProperty("system") String systemPrompt,
        @JsonProperty("max_tokens") int maxTokens,
        double temperature,
        List<Message> messages
) {
    public record Message(String role, String content) {}
}
