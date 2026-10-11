package com.hospitalfx.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
