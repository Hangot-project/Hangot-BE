package com.hanyang.adminserver.controller;

import com.hanyang.adminserver.core.response.ApiResponse;
import com.hanyang.adminserver.entity.FailedMessage;
import com.hanyang.adminserver.service.FailedMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/failed-messages")
@Tag(name = "실패 메시지 관리 API", description = "최종 실패한 메시지 관리")
public class FailedMessageController {

    private final FailedMessageService failedMessageService;

    @Operation(summary = "실패 메시지 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FailedMessage>>> getFailedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<FailedMessage> messages = failedMessageService.getFailedMessages(page, size);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @Operation(summary = "상태별 실패 메시지 조회")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<FailedMessage>>> getFailedMessagesByStatus(
            @PathVariable String status
    ) {
        List<FailedMessage> messages = failedMessageService.getFailedMessagesByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @Operation(summary = "실패 메시지 상세 조회")
    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<FailedMessage>> getFailedMessage(
            @PathVariable String messageId
    ) {
        FailedMessage message = failedMessageService.getFailedMessageById(messageId);
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @Operation(summary = "실패 메시지 처리 완료 표시")
    @PostMapping("/{messageId}/process")
    public ResponseEntity<ApiResponse<Void>> markAsProcessed(
            @PathVariable String messageId,
            @RequestParam String processedBy,
            @RequestParam(required = false) String notes
    ) {
        failedMessageService.markAsProcessed(messageId, processedBy, notes);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "실패 메시지 무시 처리")
    @PostMapping("/{messageId}/ignore")
    public ResponseEntity<ApiResponse<Void>> markAsIgnored(
            @PathVariable String messageId,
            @RequestParam String processedBy,
            @RequestParam(required = false) String notes
    ) {
        failedMessageService.markAsIgnored(messageId, processedBy, notes);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "실패 메시지 삭제")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteFailedMessage(@PathVariable String messageId) {
        failedMessageService.deleteFailedMessage(messageId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "실패 메시지 개수 조회")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFailedMessageCount() {
        long count = failedMessageService.getFailedMessageCount();
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @Operation(summary = "실패 메시지 통계")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getFailedMessageStats() {
        long totalCount = failedMessageService.getFailedMessageCount();
        long failedCount = failedMessageService.getFailedMessagesByStatus("FAILED").size();
        long processedCount = failedMessageService.getFailedMessagesByStatus("PROCESSED").size();
        long ignoredCount = failedMessageService.getFailedMessagesByStatus("IGNORED").size();
        
        var stats = new Object() {
            public final long total = totalCount;
            public final long failed = failedCount;
            public final long processed = processedCount;
            public final long ignored = ignoredCount;
        };
        
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}