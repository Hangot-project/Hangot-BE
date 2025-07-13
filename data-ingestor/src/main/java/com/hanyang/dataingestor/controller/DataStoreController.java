package com.hanyang.datastore.controller;

import com.hanyang.datastore.core.response.ApiResponse;
import com.hanyang.datastore.dto.ResAxisDto;
import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.dto.ResChartTableDto;
import com.hanyang.datastore.dto.GroupType;
import com.hanyang.datastore.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "데이터 스토어 API")
public class DataStoreController {

    private final TableService tableService;

    @Operation(summary = "파일 데이터 시각화 차트 데이터")
    @GetMapping("/dataset/{datasetId}/chart")
    public ResponseEntity<ApiResponse<ResChartDto>> chart(
            @PathVariable String datasetId, 
            @RequestParam String colName,
            @RequestParam(defaultValue = "SUM") GroupType groupType
    ) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.getAggregationLabel(datasetId, colName, groupType)));
    }

    @Operation(summary = "축 리스트 가져오기")
    @GetMapping("/dataset/{datasetId}/axis")
    public ResponseEntity<ApiResponse<ResAxisDto>> chartAxis(@PathVariable String datasetId) {
        return ResponseEntity.ok(ApiResponse.ok(new ResAxisDto(tableService.getAxis(datasetId))));
    }

    @Operation(summary = "파일 데이터 시각화 차트 데이터(테이블)")
    @GetMapping("/dataset/{datasetId}/chart/table")
    public ResponseEntity<ApiResponse<ResChartTableDto>> chart(@PathVariable String datasetId) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.getChartTable(datasetId)));
    }


}
