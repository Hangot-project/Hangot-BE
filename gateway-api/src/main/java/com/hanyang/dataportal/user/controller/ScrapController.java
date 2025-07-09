package com.hanyang.dataportal.user.controller;

import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.user.domain.Scrap;
import com.hanyang.dataportal.user.dto.res.ResIsScrapDto;
import com.hanyang.dataportal.user.dto.res.ResScrapDto;
import com.hanyang.dataportal.user.service.ScrapService;
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

    /**
     * 유저의 모든 스크랩 내역을 가져오는 메서드
     * @param userDetails
     * @return
     */
    @Operation(summary = "로그인 유저의 모든 스크랩 내역을 가져옴")
    @GetMapping("/api/scrap")
    public ResponseEntity<ApiResponse<List<ResScrapDto>>> getScraps(@AuthenticationPrincipal UserDetails userDetails) {
        List<Scrap> scrapList = scrapService.findAllByEmail(userDetails.getUsername());
        List<ResScrapDto> resScrapDtoList = scrapList
                .stream()
                .map(ResScrapDto::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(resScrapDtoList));
    }

    /**
     * 하나의 스크랩 객체를 가져오는 메서드
     * @param scrapId
     * @return
     */
    @Operation(summary = "로그인 유저의 특정 스크랩 내역을 가져옴")
    @GetMapping("/api/scrap/{scrapId}")
    public ResponseEntity<ApiResponse<ResScrapDto>> getScrap(@PathVariable Long scrapId) {
        Scrap scrap = scrapService.findByScrapId(scrapId);
        return ResponseEntity.ok(ApiResponse.ok(new ResScrapDto(scrap)));
    }

    /**
     * 스크랩 객체를 생성하는 메서드
     * @param userDetails
     * @param datasetId
     * @return
     */
    @Operation(summary = "로그인 유저의 새로운 스크랩 생성")
    @PostMapping("/api/scrap/dataset/{datasetId}")
    public ResponseEntity<ApiResponse<?>> createScrap(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long datasetId) {
        Scrap scrap = scrapService.create(userDetails.getUsername(), datasetId);
        ResScrapDto resScrapDto = new ResScrapDto(scrap);
        return ResponseEntity.ok(ApiResponse.ok(resScrapDto));
    }

    /**
     * 특정 스크랩 객체를 삭제하는 메서드
     * @param userDetails
     * @param datasetId
     * @return
     */
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
