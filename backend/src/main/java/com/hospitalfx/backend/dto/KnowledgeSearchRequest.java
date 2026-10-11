package com.hospitalfx.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record KnowledgeSearchRequest(
    @NotBlank(message = "检索内容不能为空") String query,
    @Min(value = 1, message = "topK 至少为 1")
    @Max(value = 20, message = "topK 不能超过 20")
    Integer topK
) {
}
