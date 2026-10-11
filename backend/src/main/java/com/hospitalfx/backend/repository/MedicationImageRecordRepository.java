package com.hospitalfx.backend.repository;

import com.hospitalfx.backend.model.MedicationImageRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicationImageRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public MedicationImageRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(MedicationImageRecord record) {
        jdbcTemplate.update(
            """
            INSERT INTO tb_medication_image_record(
                uploaded_by_user_id, image_hash, original_filename, content_type,
                medicine_name, specification, manufacturer, expiry_date, confidence,
                model_version, warnings, confirmation_status, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            record.getUploadedByUserId(),
            record.getImageHash(),
            record.getOriginalFilename(),
            record.getContentType(),
            record.getMedicineName(),
            record.getSpecification(),
            record.getManufacturer(),
            record.getExpiryDate(),
            record.getConfidence(),
            record.getModelVersion(),
            record.getWarnings(),
            record.getConfirmationStatus(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
