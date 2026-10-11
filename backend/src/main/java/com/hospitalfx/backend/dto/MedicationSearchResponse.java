package com.hospitalfx.backend.dto;

import java.util.List;

public record MedicationSearchResponse(
    long total,
    int page,
    int size,
    List<MedicationDetailResponse> items
) {
}
