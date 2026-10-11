package com.hospitalfx.backend.repository;

import com.hospitalfx.backend.model.KnowledgeDocument;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeDocumentRepository {

    private static final RowMapper<KnowledgeDocument> ROW_MAPPER = (rs, rowNum) -> {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(rs.getLong("id"));
        document.setTitle(rs.getString("title"));
        document.setContent(rs.getString("content"));
        document.setSourceName(rs.getString("source_name"));
        document.setSourceUrl(rs.getString("source_url"));
        document.setVersionLabel(rs.getString("version_label"));
        document.setReviewStatus(rs.getString("review_status"));
        document.setScore(rs.getDouble("score"));
        document.setCreatedAt(rs.getString("created_at"));
        document.setUpdatedAt(rs.getString("updated_at"));
        return document;
    };

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<KnowledgeDocument> search(String query, int topK) {
        return jdbcTemplate.query(
            """
            SELECT *,
                   CASE
                     WHEN LOWER(title) LIKE CONCAT('%', LOWER(?), '%') THEN 0.90
                     WHEN LOWER(content) LIKE CONCAT('%', LOWER(?), '%') THEN 0.75
                     ELSE 0.50
                   END AS score
            FROM tb_knowledge_document
            WHERE LOWER(title) LIKE CONCAT('%', LOWER(?), '%')
               OR LOWER(content) LIKE CONCAT('%', LOWER(?), '%')
               OR LOWER(COALESCE(source_name, '')) LIKE CONCAT('%', LOWER(?), '%')
            ORDER BY score DESC, updated_at DESC, id DESC
            LIMIT ?
            """,
            ROW_MAPPER,
            query,
            query,
            query,
            query,
            query,
            topK
        );
    }
}
