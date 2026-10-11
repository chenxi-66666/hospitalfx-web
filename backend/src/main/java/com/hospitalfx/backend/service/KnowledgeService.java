package com.hospitalfx.backend.service;

import com.hospitalfx.backend.dto.KnowledgeSearchItem;
import com.hospitalfx.backend.dto.KnowledgeSearchResponse;
import com.hospitalfx.backend.model.KnowledgeDocument;
import com.hospitalfx.backend.repository.KnowledgeDocumentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KnowledgeService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public KnowledgeService(KnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    public KnowledgeSearchResponse search(String query, Integer topK) {
        int safeTopK = topK == null ? DEFAULT_TOP_K : Math.max(1, Math.min(topK, MAX_TOP_K));
        List<KnowledgeDocument> documents = knowledgeDocumentRepository.search(query.trim(), safeTopK);
        if (documents.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "知识库中暂未检索到相关内容，请咨询医生 / 药师"
            );
        }

        List<KnowledgeSearchItem> answers = documents.stream()
            .map(document -> new KnowledgeSearchItem(
                document.getTitle(),
                document.getContent(),
                document.getSourceName(),
                document.getSourceUrl(),
                document.getVersionLabel(),
                document.getReviewStatus(),
                document.getScore()
            ))
            .toList();
        return new KnowledgeSearchResponse(answers);
    }
}
