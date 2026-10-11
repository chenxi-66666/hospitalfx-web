package com.hospitalfx.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicationPlanCreateRequest(
    Integer patientUserId,
    Integer registrationId,
    Integer medicationInventoryId,
    @NotBlank(message = "药品名称不能为空") String medicationName,
    String specification,
    @NotBlank(message = "用法用量不能为空") String dosage,
    @NotNull(message = "数量不能为空") @Min(value = 1, message = "数量至少为 1") Integer quantity,
    @NotBlank(message = "频次代码不能为空") String frequencyCode,
    @NotBlank(message = "频次说明不能为空") String frequencyLabel,
    @NotNull(message = "每日次数不能为空") @Min(value = 1, message = "每日次数至少为 1") Integer frequencyPerDay,
    String unit,
    String instructions
) {
}
