package com.hanyang.dataportal.notice.controller;

import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.notice.domain.Notice;
import com.hanyang.dataportal.notice.dto.req.ReqNoticeDto;
import com.hanyang.dataportal.notice.dto.res.ResNoticeDto;
import com.hanyang.dataportal.notice.dto.res.ResNoticeListDto;
import com.hanyang.dataportal.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeController {
    private final NoticeService noticeService;

    @Operation(summary = "공지사항 작성")
    @PostMapping("/notice")
    public ResponseEntity<ApiResponse<ResNoticeDto>> createNotice(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ReqNoticeDto reqNoticeDto) {
        Notice notice = noticeService.create(reqNoticeDto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new ResNoticeDto(notice)));
    }

    @Operation(summary = "공지사항 상세보기")
    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<ApiResponse<ResNoticeDto>> findNotice(@PathVariable Long noticeId) {
        Notice notice = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(ApiResponse.ok(new ResNoticeDto(notice)));
    }

    @Operation(summary = "공지사항 리스트 보기")
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<ResNoticeListDto>> findListNotice(@RequestParam Integer page) {
        Page<Notice> noticeList = noticeService.getNoticeList(page);
        return ResponseEntity.ok(ApiResponse.ok(new ResNoticeListDto(noticeList)));
    }

    @Operation(summary = "공지사항 수정")
    @PostMapping("/notice/{noticeId}")
    public ResponseEntity<ApiResponse<ResNoticeDto>> updateNotice(@PathVariable Long noticeId, @RequestBody ReqNoticeDto reqNoticeDto) {
        Notice notice = noticeService.update(reqNoticeDto, noticeId);
        return ResponseEntity.ok(ApiResponse.ok(new ResNoticeDto(notice)));
    }
    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/notice/{noticeId}")
    public ResponseEntity<ApiResponse<?>> deleteNotice(@PathVariable Long noticeId) {
        noticeService.delete(noticeId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
