package com.hanyang.api.dataset.controller;

import com.hanyang.api.core.response.ApiResponse;
import com.hanyang.api.dataset.dto.res.ResDatasetTitleDto;
import com.hanyang.api.dataset.dto.res.ResThemeListDto;
import com.hanyang.api.dataset.service.DatasetService;
import com.hanyang.api.dataset.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hanyang.api.core.response.ApiResponse.ok;

@Tag(name = "리스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ListController {

    private final DatasetService datasetService;
    private final ThemeService themeService;

    @Operation(summary = "일치하는 키워드 별 데이터셋 제목 리스트 보기")
    @GetMapping("/dataset/title")
    public ResponseEntity<ApiResponse<ResDatasetTitleDto>> getDatasetListByKeyword(@RequestParam String keyword){
        List<String> titleList = datasetService.getByKeyword(keyword);
        return ResponseEntity.ok(ok(new ResDatasetTitleDto(titleList)));
    }

    @Operation(summary = "주제 리스트 보기")
    @GetMapping("/themes")
    public ResponseEntity<ApiResponse<ResThemeListDto>> getTheme(){
        return ResponseEntity.ok(ok(new ResThemeListDto(themeService.getAllTheme())));
    }

}
