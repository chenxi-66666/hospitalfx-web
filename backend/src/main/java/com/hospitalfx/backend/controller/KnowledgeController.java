package com.hospitalfx.backend.controller;

import com.hospitalfx.backend.dto.KnowledgeSearchRequest;
import com.hospitalfx.backend.dto.KnowledgeSearchResponse;
import com.hospitalfx.backend.model.UserAccount;
import com.hospitalfx.backend.service.AuthService;
import com.hospitalfx.backend.service.KnowledgeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final AuthService authService;

    public KnowledgeController(KnowledgeService knowledgeService, AuthService authService) {
        this.knowledgeService = knowledgeService;
        this.authService = authService;
    }

    @PostMapping("/search")
    public KnowledgeSearchResponse search(
        @RequestHeader(value = "X-Auth-Token", required = false) String token,
        @Valid @RequestBody KnowledgeSearchRequest request
    ) {
        UserAccount currentUser = authService.requireUser(token);
        authService.requireAnyRole(currentUser, "ADMIN", "DOCTOR", "PHARMACIST", "PATIENT");
        return knowledgeService.search(request.query(), request.topK());
    }
}
