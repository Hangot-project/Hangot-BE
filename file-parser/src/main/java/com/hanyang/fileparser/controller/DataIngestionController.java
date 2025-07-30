package com.hanyang.fileparser.controller;

import com.hanyang.fileparser.core.exception.ParsingException;
import com.hanyang.fileparser.core.exception.ResourceNotFoundException;
import com.hanyang.fileparser.core.response.ApiResponse;
import com.hanyang.fileparser.dto.MessageDto;
import com.hanyang.fileparser.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
@Slf4j
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createDataTable(@RequestBody MessageDto messageDto) {
        try {
            dataIngestionService.createDataTable(messageDto);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (ResourceNotFoundException | IllegalArgumentException | ParsingException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail(e.getMessage()));
        }
    }
}