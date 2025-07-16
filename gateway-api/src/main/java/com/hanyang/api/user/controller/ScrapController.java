package com.hanyang.api.user.controller;

import com.hanyang.api.core.response.ApiResponse;
import com.hanyang.api.user.domain.Scrap;
import com.hanyang.api.user.dto.ResIsScrapDto;
import com.hanyang.api.user.dto.ResScrapDto;
import com.hanyang.api.user.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "스크랩 API")
public class ScrapController {
    private final ScrapService scrapService;

    @Operation(summary = "로그인 유저의 모든 스크랩 내역을 가져옴")
    @GetMapping("/api/scrap")
    public ResponseEntity<ApiResponse<List<ResScrapDto>>> getScraps(@AuthenticationPrincipal UserDetails userDetails) {
        List<Scrap> scrapList = scrapService.findAllByProviderId(userDetails.getUsername());
        List<ResScrapDto> resScrapDtoList = scrapList
                .stream()
                .map(ResScrapDto::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(resScrapDtoList));
    }

    @Operation(summary = "로그인 유저의 특정 스크랩 내역을 가져옴")
    @GetMapping("/api/scrap/{scrapId}")
    public ResponseEntity<ApiResponse<ResScrapDto>> getScrap(@PathVariable Long scrapId) {
        Scrap scrap = scrapService.findByScrapId(scrapId);
        return ResponseEntity.ok(ApiResponse.ok(new ResScrapDto(scrap)));
    }

    @Operation(summary = "로그인 유저의 새로운 스크랩 생성")
    @PostMapping("/api/scrap/dataset/{datasetId}")
    public ResponseEntity<ApiResponse<ResScrapDto>> createScrap(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long datasetId) {
        Scrap scrap = scrapService.scrap(userDetails.getUsername(), datasetId);
        ResScrapDto resScrapDto = new ResScrapDto(scrap);
        return ResponseEntity.ok(ApiResponse.ok(resScrapDto));
    }

    @Operation(summary = "로그인 유저의 특정 스크랩 내역 삭제")
    @DeleteMapping("/api/scrap/dataset/{datasetId}")
    public ResponseEntity<ApiResponse<?>> deleteScrap(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long datasetId) {
        scrapService.delete(userDetails.getUsername(), datasetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "특정 데이터셋에 대한 유저 스크랩 여부")
    @GetMapping("/api/scrap/dataset/{datasetId}")
    public ResponseEntity<ApiResponse<?>> isScrap(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long datasetId) {
        return ResponseEntity.ok(ApiResponse.ok(new ResIsScrapDto(scrapService.isUserScrap(userDetails.getUsername(),datasetId))));
    }
}
