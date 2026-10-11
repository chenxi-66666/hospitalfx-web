package com.hospitalfx.backend.dto;

import java.util.List;

public record KnowledgeSearchResponse(
    List<KnowledgeSearchItem> answers
) {
}
