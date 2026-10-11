package com.hospitalfx.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public record MedicineImageResponse(
    String medicineName,
    String specification,
    String manufacturer,
    String expiryDate,
    Double confidence,
    String modelVersion,
    List<String> warnings
) {
}
