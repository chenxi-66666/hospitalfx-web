package com.hospitalfx.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record MedicationDetailResponse(
    Integer id,
    String medicationCode,
    String medicationName,
    String specification,
    String unit,
    BigDecimal unitPrice,
    Integer stockQuantity,
    Integer safeStock,
    String usageNotes,
    String indications,
    String contraindications,
    String sourceName,
    String sourceUrl,
    String versionLabel,
    String updatedAt
) {
}
