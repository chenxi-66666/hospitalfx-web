package com.hospitalfx.backend.service;

import com.hospitalfx.backend.dto.MedicationPlanCreateRequest;
import com.hospitalfx.backend.model.MedicationInventory;
import com.hospitalfx.backend.model.MedicationPlan;
import com.hospitalfx.backend.model.RegistrationRecord;
import com.hospitalfx.backend.model.UserAccount;
import com.hospitalfx.backend.repository.MedicationInventoryRepository;
import com.hospitalfx.backend.repository.MedicationPlanRepository;
import com.hospitalfx.backend.repository.RegistrationRepository;
import com.hospitalfx.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MedicationPlanService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final MedicationPlanRepository medicationPlanRepository;
    private final MedicationInventoryRepository medicationInventoryRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public MedicationPlanService(
        MedicationPlanRepository medicationPlanRepository,
        MedicationInventoryRepository medicationInventoryRepository,
        RegistrationRepository registrationRepository,
        UserRepository userRepository
    ) {
        this.medicationPlanRepository = medicationPlanRepository;
        this.medicationInventoryRepository = medicationInventoryRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    public List<MedicationPlan> listPlans(UserAccount currentUser, Integer patientUserId) {
        Integer targetPatientUserId = resolveTargetPatientUserId(currentUser, patientUserId);
        return medicationPlanRepository.findByPatientUserId(targetPatientUserId);
    }

    public MedicationPlan createPlan(UserAccount currentUser, MedicationPlanCreateRequest request) {
        Integer patientUserId = resolveTargetPatientUserId(currentUser, request.patientUserId());
        UserAccount patient = userRepository.findById(patientUserId)
            .filter(user -> "PATIENT".equals(user.getRoleCode()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "用药计划只能关联患者账号"));

        MedicationInventory inventory = resolveInventory(request);
        RegistrationRecord registration = resolveRegistration(patientUserId, request.registrationId());
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        MedicationPlan plan = new MedicationPlan();
        plan.setPatientUserId(patient.getId());
        plan.setRegistrationId(registration.getId());
        plan.setMedicationInventoryId(inventory.getId());
        plan.setMedicationName(inventory.getMedicationName());
        plan.setDosage(request.dosage().trim());
        plan.setQuantity(request.quantity());
        plan.setFrequencyCode(request.frequencyCode().trim());
        plan.setFrequencyLabel(request.frequencyLabel().trim());
        plan.setFrequencyPerDay(request.frequencyPerDay());
        plan.setUnit(blankToDefault(request.unit(), inventory.getUnit()));
        plan.setInstructions(blankToDefault(request.instructions(), inventory.getUsageNotes()));
        plan.setStatus("ACTIVE");
        plan.setNextReminderAt(nextReminderAt(request.frequencyCode()));
        plan.setLastCheckedInAt(null);
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        return medicationPlanRepository.create(plan);
    }

    private Integer resolveTargetPatientUserId(UserAccount currentUser, Integer patientUserId) {
        if ("PATIENT".equals(currentUser.getRoleCode())) {
            if (patientUserId != null && !currentUser.getId().equals(patientUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能访问自己的用药计划");
            }
            return currentUser.getId();
        }

        if (patientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请提供 patientUserId");
        }
        return patientUserId;
    }

    private MedicationInventory resolveInventory(MedicationPlanCreateRequest request) {
        if (request.medicationInventoryId() != null) {
            return medicationInventoryRepository.findById(request.medicationInventoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "药品库存数据不存在"));
        }

        return medicationInventoryRepository.findFirstByName(request.medicationName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "未找到匹配的药品库存"));
    }

    private RegistrationRecord resolveRegistration(Integer patientUserId, Integer registrationId) {
        if (registrationId != null) {
            RegistrationRecord registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "挂号记录不存在"));
            if (!patientUserId.equals(registration.getPatientUserId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "挂号记录与患者不匹配");
            }
            return registration;
        }

        return registrationRepository.findLatestByPatientUserId(patientUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "未找到该患者可关联的挂号记录"));
    }

    private String nextReminderAt(String frequencyCode) {
        return LocalDateTime.now()
            .plusHours(resolveReminderHours(frequencyCode))
            .format(DATE_TIME_FORMATTER);
    }

    private long resolveReminderHours(String frequencyCode) {
        return switch (frequencyCode == null ? "" : frequencyCode.trim()) {
            case "TID" -> 8L;
            case "BID" -> 12L;
            case "HS", "QD" -> 24L;
            default -> 24L;
        };
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
