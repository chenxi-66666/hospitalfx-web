package com.hospitalfx.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitalfx.backend.dto.MedicineImageResponse;
import com.hospitalfx.backend.model.AiInferenceRecord;
import com.hospitalfx.backend.model.MedicationImageRecord;
import com.hospitalfx.backend.model.UserAccount;
import com.hospitalfx.backend.repository.AiInferenceRecordRepository;
import com.hospitalfx.backend.repository.MedicationImageRecordRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MedicineImageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final MedicationImageRecordRepository medicationImageRecordRepository;
    private final AiInferenceRecordRepository aiInferenceRecordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Value("${ai-service.base-url:http://localhost:8090}")
    private String aiServiceBaseUrl;

    public MedicineImageService(
        MedicationImageRecordRepository medicationImageRecordRepository,
        AiInferenceRecordRepository aiInferenceRecordRepository
    ) {
        this.medicationImageRecordRepository = medicationImageRecordRepository;
        this.aiInferenceRecordRepository = aiInferenceRecordRepository;
    }

    public MedicineImageResponse recognize(UserAccount currentUser, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择要识别的药品图片");
        }
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "图片大小不能超过 5 MB");
        }

        String contentType = resolveContentType(image);
        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "读取上传图片失败", error);
        }

        String imageHash = sha256(imageBytes);
        String filename = sanitizeFilename(image.getOriginalFilename());
        long startedAt = System.nanoTime();
        MedicineImageResponse response = requestAiService(imageBytes, filename, contentType);
        int durationMs = Math.toIntExact(Math.min(Integer.MAX_VALUE, (System.nanoTime() - startedAt) / 1_000_000L));

        recordAudit(currentUser, filename, contentType, imageHash, response, durationMs);
        return response;
    }

    private MedicineImageResponse requestAiService(byte[] imageBytes, String filename, String contentType) {
        String boundary = "----HospitalFx" + UUID.randomUUID().toString().replace("-", "");
        byte[] requestBody;
        try {
            requestBody = buildMultipartBody(boundary, imageBytes, filename, contentType);
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "构建图片上传请求失败", error);
        }

        String baseUrl = aiServiceBaseUrl == null ? "" : aiServiceBaseUrl.replaceAll("/+$", "");
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/predict/medicine-image"))
            .timeout(Duration.ofSeconds(30))
            .version(HttpClient.Version.HTTP_1_1)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
            .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 图片识别服务暂时不可用");
            }

            MedicineImageResponse parsed = objectMapper.readValue(response.body(), MedicineImageResponse.class);
            return new MedicineImageResponse(
                parsed.medicineName(),
                parsed.specification(),
                parsed.manufacturer(),
                parsed.expiryDate(),
                parsed.confidence(),
                parsed.modelVersion(),
                parsed.warnings() == null ? List.of() : parsed.warnings()
            );
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 图片识别服务暂时不可用", error);
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 图片识别服务暂时不可用", error);
        }
    }

    private byte[] buildMultipartBody(String boundary, byte[] imageBytes, String filename, String contentType) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String header = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"\r\n"
            + "Content-Type: " + contentType + "\r\n\r\n";
        output.write(header.getBytes(StandardCharsets.UTF_8));
        output.write(imageBytes);
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private String resolveContentType(MultipartFile image) {
        String filename = sanitizeFilename(image.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String declared = image.getContentType() == null ? "" : image.getContentType().toLowerCase(Locale.ROOT);

        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (filename.endsWith(".png")) {
            return "image/png";
        }
        if (filename.endsWith(".webp")) {
            return "image/webp";
        }
        if (List.of("image/jpeg", "image/jpg", "image/png", "image/webp").contains(declared)) {
            return "image/jpg".equals(declared) ? "image/jpeg" : declared;
        }

        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "图片格式仅支持 jpg、jpeg、png、webp");
    }

    private void recordAudit(
        UserAccount currentUser,
        String filename,
        String contentType,
        String imageHash,
        MedicineImageResponse response,
        int durationMs
    ) {
        try {
            String createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            MedicationImageRecord imageRecord = new MedicationImageRecord();
            imageRecord.setUploadedByUserId(currentUser.getId());
            imageRecord.setImageHash(imageHash);
            imageRecord.setOriginalFilename(truncate(filename, 255));
            imageRecord.setContentType(truncate(contentType, 100));
            imageRecord.setMedicineName(truncate(response.medicineName(), 80));
            imageRecord.setSpecification(truncate(response.specification(), 80));
            imageRecord.setManufacturer(truncate(response.manufacturer(), 120));
            imageRecord.setExpiryDate(truncate(response.expiryDate(), 20));
            imageRecord.setConfidence(toBigDecimal(response.confidence()));
            imageRecord.setModelVersion(blankToDefault(response.modelVersion(), "unknown"));
            imageRecord.setWarnings(truncate(objectMapper.writeValueAsString(response.warnings()), 2000));
            imageRecord.setConfirmationStatus(hasLowConfidence(response.confidence()) ? "PENDING" : "AUTO_ACCEPTED");
            imageRecord.setCreatedAt(createdAt);
            medicationImageRecordRepository.create(imageRecord);

            AiInferenceRecord inferenceRecord = new AiInferenceRecord();
            inferenceRecord.setModelType("MEDICINE_IMAGE");
            inferenceRecord.setInputSummary(truncate("imageHash=" + imageHash + ", filename=" + filename, 500));
            inferenceRecord.setOutputSummary(truncate(objectMapper.writeValueAsString(response), 1000));
            inferenceRecord.setModelVersion(blankToDefault(response.modelVersion(), "unknown"));
            inferenceRecord.setConfidence(toBigDecimal(response.confidence()));
            inferenceRecord.setDurationMs(durationMs);
            inferenceRecord.setCreatedAt(createdAt);
            aiInferenceRecordRepository.create(inferenceRecord);
        } catch (Exception error) {
            // Audit logging must not hide a successful recognition response.
            System.err.println("Failed to record medicine image audit: " + error.getMessage());
        }
    }

    private boolean hasLowConfidence(Double confidence) {
        return confidence == null || confidence < 0.6;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is not available", error);
        }
    }

    private String sanitizeFilename(String filename) {
        String value = blankToDefault(filename, "medicine-image.jpg");
        return value.replace("\\", "_").replace("\"", "'");
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
