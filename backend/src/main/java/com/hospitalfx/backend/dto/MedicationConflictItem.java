package com.hospitalfx.backend.dto;

public record MedicationConflictItem(
    String leftMedicationName,
    String rightMedicationName,
    String severity,
    String guidance
) {
}
