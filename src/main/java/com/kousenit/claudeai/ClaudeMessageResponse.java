package com.kousenit.claudeai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClaudeMessageResponse(String id,
                                    String type,
                                    String role,
                                    String model,
                                    @JsonProperty("stop_reason") String stopReason,
                                    @JsonProperty("stop_sequence") String stopSequence,
                                    List<Content> content,
                                    Usage usage) {
    public record Content(String type, String text) {}
    public record Usage(@JsonProperty("input_tokens") int inputTokens,
                        @JsonProperty("output_tokens") int outputTokens) {}
}
