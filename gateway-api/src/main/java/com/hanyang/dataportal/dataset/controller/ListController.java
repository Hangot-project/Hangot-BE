package com.hanyang.dataportal.dataset.controller;

import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.dataset.domain.vo.License;
import com.hanyang.dataportal.dataset.dto.res.*;
import com.hanyang.dataportal.dataset.service.DatasetService;
import com.hanyang.dataportal.dataset.service.OrganizationService;
import com.hanyang.dataportal.dataset.service.ThemeService;
import com.hanyang.dataportal.qna.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hanyang.dataportal.core.response.ApiResponse.ok;

@Tag(name = "리스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ListController {

    private final OrganizationService organizationService;
    private final DatasetService datasetService;
    private final ThemeService themeService;
    private final QuestionService questionService;

    @Operation(summary = "일치하는 키워드 별 조직 리스트 보기")
    @GetMapping("/organizations")
    public ResponseEntity<ApiResponse<ResOrganizationDto>> findOrganization(@RequestParam(required = false,defaultValue = "") String keyword){
        return ResponseEntity.ok(ok(new ResOrganizationDto(organizationService.findByKeyword(keyword))));
    }
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

    @Operation(summary = "질문 카테고리 리스트 보기")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<ResQuestionCategoryListDto>> getCategory(){
        return ResponseEntity.ok(ok(new ResQuestionCategoryListDto(questionService.getQuestionCategoryList())));
    }

    @Operation(summary = "라이센스 리스트 보기")
    @GetMapping("/licenses")
    public ResponseEntity<ApiResponse<ResLicenseListDto>> getLicense(){
        List<License> licenses = new ArrayList<>();
        Collections.addAll(licenses, License.values());
        return ResponseEntity.ok(ok(new ResLicenseListDto(licenses)));
    }

}
