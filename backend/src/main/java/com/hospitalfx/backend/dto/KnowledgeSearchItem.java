package com.hospitalfx.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record KnowledgeSearchItem(
    String title,
    String content,
    String sourceName,
    String sourceUrl,
    String versionLabel,
    String reviewStatus,
    Double score
) {
}
