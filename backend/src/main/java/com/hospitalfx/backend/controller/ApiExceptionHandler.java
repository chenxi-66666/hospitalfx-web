package com.hospitalfx.backend.controller;

import com.hospitalfx.backend.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
        ResponseStatusException error,
        HttpServletRequest request
    ) {
        int status = error.getStatusCode().value();
        String message = error.getReason() == null || error.getReason().isBlank()
            ? "请求处理失败"
            : error.getReason();
        return buildResponse(status, message, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException error,
        HttpServletRequest request
    ) {
        String message = error.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("请求参数不正确");
        return buildResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
        MaxUploadSizeExceededException error,
        HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), "图片大小不能超过 5 MB", request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
        HttpMediaTypeNotSupportedException error,
        HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "请求格式不受支持", request.getRequestURI());
    }

    @ExceptionHandler({
        MissingServletRequestPartException.class,
        MissingServletRequestParameterException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception error, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST.value(), "请求参数不正确", request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(int status, String message, String path) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        ApiErrorResponse response = new ApiErrorResponse(
            OffsetDateTime.now().toString(),
            status,
            httpStatus == null ? "Error" : httpStatus.getReasonPhrase(),
            message,
            path
        );
        return ResponseEntity.status(status).body(response);
    }
}
