package com.hospitalfx.backend.controller;

import com.hospitalfx.backend.dto.MedicationConflictCheckRequest;
import com.hospitalfx.backend.dto.MedicationConflictCheckResponse;
import com.hospitalfx.backend.model.UserAccount;
import com.hospitalfx.backend.service.AuthService;
import com.hospitalfx.backend.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medication-conflicts")
public class MedicationConflictController {

    private final MedicationService medicationService;
    private final AuthService authService;

    public MedicationConflictController(MedicationService medicationService, AuthService authService) {
        this.medicationService = medicationService;
        this.authService = authService;
    }

    @PostMapping("/check")
    public MedicationConflictCheckResponse check(
        @RequestHeader(value = "X-Auth-Token", required = false) String token,
        @Valid @RequestBody MedicationConflictCheckRequest request
    ) {
        UserAccount currentUser = authService.requireUser(token);
        authService.requireAnyRole(currentUser, "ADMIN", "DOCTOR", "PHARMACIST", "PATIENT");
        return medicationService.checkConflicts(request.medicationNames());
    }
}
