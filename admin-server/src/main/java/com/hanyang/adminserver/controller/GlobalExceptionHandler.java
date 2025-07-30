package com.hanyang.adminserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanyang.adminserver.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonProcessingException(JsonProcessingException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error("JSON 처리 오류: " + e.getMessage()));
    }
}