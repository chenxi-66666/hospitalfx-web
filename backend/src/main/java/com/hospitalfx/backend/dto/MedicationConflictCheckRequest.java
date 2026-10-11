package com.hospitalfx.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MedicationConflictCheckRequest(
    @NotEmpty(message = "请至少提供一种药品名称")
    List<@NotBlank(message = "药品名称不能为空") String> medicationNames
) {
}
