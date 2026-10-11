package com.hospitalfx.backend.dto;

import java.util.List;

public record MedicationConflictCheckResponse(
    boolean hasConflict,
    List<MedicationConflictItem> conflicts,
    String modelVersion
) {
}
