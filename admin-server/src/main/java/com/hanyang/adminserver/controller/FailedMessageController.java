package com.hanyang.adminserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanyang.adminserver.response.ApiResponse;
import com.hanyang.adminserver.entity.FailedMessage;
import com.hanyang.adminserver.service.FailedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/failed-messages")
public class FailedMessageController {

    private final FailedMessageService failedMessageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FailedMessage>>> getFailedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<FailedMessage> messages = failedMessageService.getFailedMessages(page, size);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }


    @PostMapping("/{messageId}/process")
    public ResponseEntity<ApiResponse<Void>> retryProcessing(
            @PathVariable String messageId,
            @RequestBody(required = false) ProcessRequest processRequest
    ) throws JsonProcessingException {
        String editedMessage = processRequest != null ? processRequest.getEditedMessage() : null;
        failedMessageService.retryProcessing(messageId, editedMessage);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{messageId}/ignore")
    public ResponseEntity<ApiResponse<Void>> markAsIgnored(
            @PathVariable String messageId
    ) {
        failedMessageService.markAsIgnored(messageId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteFailedMessage(@PathVariable String messageId) {
        failedMessageService.deleteFailedMessage(messageId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{messageId}/notes")
    public ResponseEntity<ApiResponse<Void>> updateNotes(
            @PathVariable String messageId,
            @RequestParam String notes
    ) {
        failedMessageService.updateNotes(messageId, notes);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}