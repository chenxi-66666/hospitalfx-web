package com.hospitalfx.backend.service;

import com.hospitalfx.backend.dto.MedicationConflictCheckResponse;
import com.hospitalfx.backend.dto.MedicationConflictItem;
import com.hospitalfx.backend.dto.MedicationDetailResponse;
import com.hospitalfx.backend.dto.MedicationSearchResponse;
import com.hospitalfx.backend.model.MedicationConflict;
import com.hospitalfx.backend.model.MedicationInventory;
import com.hospitalfx.backend.model.MedicationKnowledge;
import com.hospitalfx.backend.repository.MedicationConflictRepository;
import com.hospitalfx.backend.repository.MedicationInventoryRepository;
import com.hospitalfx.backend.repository.MedicationKnowledgeRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MedicationService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CONFLICT_MODEL_VERSION = "rule-baseline-0.1";

    private final MedicationInventoryRepository medicationInventoryRepository;
    private final MedicationKnowledgeRepository medicationKnowledgeRepository;
    private final MedicationConflictRepository medicationConflictRepository;

    public MedicationService(
        MedicationInventoryRepository medicationInventoryRepository,
        MedicationKnowledgeRepository medicationKnowledgeRepository,
        MedicationConflictRepository medicationConflictRepository
    ) {
        this.medicationInventoryRepository = medicationInventoryRepository;
        this.medicationKnowledgeRepository = medicationKnowledgeRepository;
        this.medicationConflictRepository = medicationConflictRepository;
    }

    public MedicationDetailResponse getMedication(Integer id) {
        MedicationInventory inventory = medicationInventoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该药品"));
        MedicationKnowledge knowledge = medicationKnowledgeRepository.findByMedicationInventoryId(id).orElse(null);
        return toDetail(inventory, knowledge);
    }

    public MedicationSearchResponse searchMedications(String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        int offset = (safePage - 1) * safeSize;
        long total = medicationInventoryRepository.count(keyword);
        List<MedicationInventory> inventories = medicationInventoryRepository.search(keyword, safeSize, offset);
        Map<Integer, MedicationKnowledge> knowledgeByInventoryId = loadKnowledge(inventories);
        List<MedicationDetailResponse> items = inventories.stream()
            .map(inventory -> toDetail(inventory, knowledgeByInventoryId.get(inventory.getId())))
            .toList();
        return new MedicationSearchResponse(total, safePage, safeSize, items);
    }

    public MedicationConflictCheckResponse checkConflicts(List<String> medicationNames) {
        List<String> normalizedNames = medicationNames == null
            ? List.of()
            : medicationNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (normalizedNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请至少提供一种药品名称");
        }

        List<MedicationConflictItem> conflicts = medicationConflictRepository.findByMedicationNames(normalizedNames)
            .stream()
            .map(this::toConflictItem)
            .toList();
        return new MedicationConflictCheckResponse(!conflicts.isEmpty(), conflicts, CONFLICT_MODEL_VERSION);
    }

    private Map<Integer, MedicationKnowledge> loadKnowledge(List<MedicationInventory> inventories) {
        if (inventories.isEmpty()) {
            return Map.of();
        }

        Map<Integer, MedicationKnowledge> result = new LinkedHashMap<>();
        List<Integer> ids = inventories.stream().map(MedicationInventory::getId).toList();
        medicationKnowledgeRepository.findByMedicationInventoryIds(ids)
            .forEach(knowledge -> result.put(knowledge.getMedicationInventoryId(), knowledge));
        return result;
    }

    private MedicationDetailResponse toDetail(MedicationInventory inventory, MedicationKnowledge knowledge) {
        return new MedicationDetailResponse(
            inventory.getId(),
            inventory.getMedicationCode(),
            inventory.getMedicationName(),
            inventory.getSpecification(),
            inventory.getUnit(),
            inventory.getUnitPrice(),
            inventory.getStockQuantity(),
            inventory.getSafeStock(),
            inventory.getUsageNotes(),
            knowledge == null ? null : knowledge.getIndications(),
            knowledge == null ? null : knowledge.getContraindications(),
            knowledge == null ? null : knowledge.getSourceName(),
            knowledge == null ? null : knowledge.getSourceUrl(),
            knowledge == null ? null : knowledge.getVersionLabel(),
            knowledge == null ? null : knowledge.getUpdatedAt()
        );
    }

    private MedicationConflictItem toConflictItem(MedicationConflict conflict) {
        return new MedicationConflictItem(
            conflict.getLeftMedicationName(),
            conflict.getRightMedicationName(),
            conflict.getSeverity(),
            conflict.getGuidance()
        );
    }
}
