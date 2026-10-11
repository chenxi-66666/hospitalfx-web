package com.hospitalfx.backend.repository;

import com.hospitalfx.backend.model.AiInferenceRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AiInferenceRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public AiInferenceRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(AiInferenceRecord record) {
        jdbcTemplate.update(
            """
            INSERT INTO tb_ai_inference_record(
                model_type, input_summary, output_summary, model_version, confidence, duration_ms, created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            record.getModelType(),
            record.getInputSummary(),
            record.getOutputSummary(),
            record.getModelVersion(),
            record.getConfidence(),
            record.getDurationMs(),
            record.getCreatedAt()
        );
    }
}
