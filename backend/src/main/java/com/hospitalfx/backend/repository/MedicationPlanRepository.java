package com.hospitalfx.backend.repository;

import com.hospitalfx.backend.model.MedicationPlan;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MedicationPlanRepository {

    private static final RowMapper<MedicationPlan> ROW_MAPPER = (rs, rowNum) -> {
        MedicationPlan plan = new MedicationPlan();
        plan.setId(rs.getInt("id"));
        plan.setPatientUserId(rs.getInt("patient_user_id"));
        plan.setRegistrationId(rs.getInt("registration_id"));
        plan.setMedicationInventoryId(rs.getInt("medication_inventory_id"));
        plan.setMedicationName(rs.getString("medication_name"));
        plan.setDosage(rs.getString("dosage"));
        plan.setQuantity(rs.getInt("quantity"));
        plan.setFrequencyCode(rs.getString("frequency_code"));
        plan.setFrequencyLabel(rs.getString("frequency_label"));
        plan.setFrequencyPerDay(rs.getInt("frequency_per_day"));
        plan.setUnit(rs.getString("unit"));
        plan.setInstructions(rs.getString("instructions"));
        plan.setStatus(rs.getString("status"));
        plan.setNextReminderAt(rs.getString("next_reminder_at"));
        plan.setLastCheckedInAt(rs.getString("last_checked_in_at"));
        plan.setCreatedAt(rs.getString("created_at"));
        plan.setUpdatedAt(rs.getString("updated_at"));
        return plan;
    };

    private final JdbcTemplate jdbcTemplate;

    public MedicationPlanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MedicationPlan> findAll() {
        return jdbcTemplate.query("SELECT * FROM tb_medication_plan ORDER BY id", ROW_MAPPER);
    }

    public Optional<MedicationPlan> findById(Integer id) {
        return jdbcTemplate.query("SELECT * FROM tb_medication_plan WHERE id = ?", ROW_MAPPER, id).stream().findFirst();
    }

    public List<MedicationPlan> findByRegistrationId(Integer registrationId) {
        return jdbcTemplate.query("SELECT * FROM tb_medication_plan WHERE registration_id = ? ORDER BY id", ROW_MAPPER, registrationId);
    }

    public List<MedicationPlan> findByPatientUserId(Integer patientUserId) {
        return jdbcTemplate.query(
            "SELECT * FROM tb_medication_plan WHERE patient_user_id = ? ORDER BY id",
            ROW_MAPPER,
            patientUserId
        );
    }

    public void deleteByRegistrationId(Integer registrationId) {
        jdbcTemplate.update("DELETE FROM tb_medication_plan WHERE registration_id = ?", registrationId);
    }

    public MedicationPlan create(MedicationPlan plan) {
        String sql =
            """
            INSERT INTO tb_medication_plan(
                patient_user_id, registration_id, medication_inventory_id, medication_name, dosage,
                quantity, frequency_code, frequency_label, frequency_per_day, unit, instructions,
                status, next_reminder_at, last_checked_in_at, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, plan.getPatientUserId());
            statement.setObject(2, plan.getRegistrationId());
            statement.setObject(3, plan.getMedicationInventoryId());
            statement.setObject(4, plan.getMedicationName());
            statement.setObject(5, plan.getDosage());
            statement.setObject(6, plan.getQuantity());
            statement.setObject(7, plan.getFrequencyCode());
            statement.setObject(8, plan.getFrequencyLabel());
            statement.setObject(9, plan.getFrequencyPerDay());
            statement.setObject(10, plan.getUnit());
            statement.setObject(11, plan.getInstructions());
            statement.setObject(12, plan.getStatus());
            statement.setObject(13, plan.getNextReminderAt());
            statement.setObject(14, plan.getLastCheckedInAt());
            statement.setObject(15, plan.getCreatedAt());
            statement.setObject(16, plan.getUpdatedAt());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            plan.setId(generatedId.intValue());
        }
        return plan;
    }

    public void activateByRegistrationId(Integer registrationId) {
        jdbcTemplate.update(
            """
            UPDATE tb_medication_plan
            SET status = 'ACTIVE'
            WHERE registration_id = ? AND status = 'PENDING_PICKUP'
            """,
            registrationId
        );
    }

    public void markCheckedIn(Integer id, String checkedInAt, String nextReminderAt) {
        jdbcTemplate.update(
            """
            UPDATE tb_medication_plan
            SET status = 'ACTIVE',
                last_checked_in_at = ?,
                next_reminder_at = ?
            WHERE id = ?
            """,
            checkedInAt,
            nextReminderAt,
            id
        );
    }
}
