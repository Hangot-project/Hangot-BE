package com.hanyang.dataportal.dataset.controller;

import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.dto.req.ReqDataSearchDto;
import com.hanyang.dataportal.dataset.dto.res.ResDatasetDetailDto;
import com.hanyang.dataportal.dataset.dto.res.ResDatasetListDto;
import com.hanyang.dataportal.dataset.dto.res.ResDatasetMainDto;
import com.hanyang.dataportal.dataset.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "데이터셋 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DatasetController {
    private final DatasetService datasetService;

    @Operation(summary = "dataset 리스트 보기")
    @GetMapping("/datasets")
    public ResponseEntity<ApiResponse<ResDatasetListDto>> getDatasetList(ReqDataSearchDto reqDataSearchDto){
        Page<Dataset> datasetList = datasetService.getDatasetList(reqDataSearchDto);
        return ResponseEntity.ok(ApiResponse.ok(new ResDatasetListDto(datasetList)));
    }

    @Operation(summary = "dataset 상세 보기")
    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<ApiResponse<ResDatasetDetailDto>> getDataset(@PathVariable Long datasetId){
        return ResponseEntity.ok(ApiResponse.ok(datasetService.getDatasetDetail(datasetId)));
    }
    @Operation(summary = "인기 데이터 리스트가져오기")
    @GetMapping(value = "/dataset/popular")
    public ResponseEntity<ApiResponse<ResDatasetMainDto>> popularData() {
        return ResponseEntity.ok(ApiResponse.ok(new ResDatasetMainDto(datasetService.getPopular())));
    }

    @Operation(summary = "신규 데이터 리스트가져오기")
    @GetMapping(value = "/dataset/new")
    public ResponseEntity<ApiResponse<ResDatasetMainDto>> newData() {
        return ResponseEntity.ok(ApiResponse.ok(new ResDatasetMainDto(datasetService.getNew())));
    }


}
