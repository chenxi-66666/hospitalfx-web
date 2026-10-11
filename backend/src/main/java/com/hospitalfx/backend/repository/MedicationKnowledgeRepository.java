package com.hospitalfx.backend.repository;

import com.hospitalfx.backend.model.MedicationKnowledge;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MedicationKnowledgeRepository {

    private static final RowMapper<MedicationKnowledge> ROW_MAPPER = (rs, rowNum) -> {
        MedicationKnowledge knowledge = new MedicationKnowledge();
        knowledge.setId(rs.getLong("id"));
        knowledge.setMedicationInventoryId(rs.getInt("medication_inventory_id"));
        knowledge.setMedicationCode(rs.getString("medication_code"));
        knowledge.setMedicationName(rs.getString("medication_name"));
        knowledge.setGenericName(rs.getString("generic_name"));
        knowledge.setIndications(rs.getString("indications"));
        knowledge.setContraindications(rs.getString("contraindications"));
        knowledge.setAdverseReactions(rs.getString("adverse_reactions"));
        knowledge.setSourceName(rs.getString("source_name"));
        knowledge.setSourceUrl(rs.getString("source_url"));
        knowledge.setVersionLabel(rs.getString("version_label"));
        knowledge.setReviewStatus(rs.getString("review_status"));
        knowledge.setCreatedAt(rs.getString("created_at"));
        knowledge.setUpdatedAt(rs.getString("updated_at"));
        return knowledge;
    };

    private final JdbcTemplate jdbcTemplate;

    public MedicationKnowledgeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<MedicationKnowledge> findByMedicationInventoryId(Integer medicationInventoryId) {
        return jdbcTemplate.query(
            "SELECT * FROM tb_medication_knowledge WHERE medication_inventory_id = ?",
            ROW_MAPPER,
            medicationInventoryId
        ).stream().findFirst();
    }

    public List<MedicationKnowledge> findByMedicationInventoryIds(List<Integer> medicationInventoryIds) {
        if (medicationInventoryIds == null || medicationInventoryIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(", ", Collections.nCopies(medicationInventoryIds.size(), "?"));
        return jdbcTemplate.query(
            "SELECT * FROM tb_medication_knowledge WHERE medication_inventory_id IN (" + placeholders + ")",
            ROW_MAPPER,
            medicationInventoryIds.toArray()
        );
    }
}
