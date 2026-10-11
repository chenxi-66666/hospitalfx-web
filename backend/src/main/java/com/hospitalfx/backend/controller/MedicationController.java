package com.hospitalfx.backend.controller;

import com.hospitalfx.backend.dto.MedicationDetailResponse;
import com.hospitalfx.backend.dto.MedicationSearchResponse;
import com.hospitalfx.backend.model.UserAccount;
import com.hospitalfx.backend.service.AuthService;
import com.hospitalfx.backend.service.MedicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;
    private final AuthService authService;

    public MedicationController(MedicationService medicationService, AuthService authService) {
        this.medicationService = medicationService;
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public MedicationDetailResponse getMedication(
        @RequestHeader(value = "X-Auth-Token", required = false) String token,
        @PathVariable Integer id
    ) {
        requireLoggedInUser(token);
        return medicationService.getMedication(id);
    }

    @GetMapping
    public MedicationSearchResponse searchMedications(
        @RequestHeader(value = "X-Auth-Token", required = false) String token,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        requireLoggedInUser(token);
        return medicationService.searchMedications(keyword, page, size);
    }

    private UserAccount requireLoggedInUser(String token) {
        UserAccount currentUser = authService.requireUser(token);
        authService.requireAnyRole(currentUser, "ADMIN", "DOCTOR", "PHARMACIST", "PATIENT");
        return currentUser;
    }
}
