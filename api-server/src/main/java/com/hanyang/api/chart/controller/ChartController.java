package com.hanyang.api.chart.controller;

import com.hanyang.api.chart.dto.GroupType;
import com.hanyang.api.chart.dto.ResAxisDto;
import com.hanyang.api.chart.dto.ResChartDto;
import com.hanyang.api.chart.dto.ResChartTableDto;
import com.hanyang.api.chart.service.ChartService;
import com.hanyang.api.core.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/datasets")
public class ChartController {

    private final ChartService chartService;

    @GetMapping("/{datasetId}/chart")
    public ResponseEntity<ApiResponse<ResChartDto>> chart(
            @PathVariable String datasetId, 
            @RequestParam String colName,
            @RequestParam(defaultValue = "SUM") GroupType groupType
    ) {
        return ResponseEntity.ok(ApiResponse.ok(chartService.getAggregationLabel(datasetId, colName, groupType)));
    }

    @GetMapping("/{datasetId}/axis")
    public ResponseEntity<ApiResponse<ResAxisDto>> chartAxis(@PathVariable String datasetId) {
        return ResponseEntity.ok(ApiResponse.ok(new ResAxisDto(chartService.getAxis(datasetId))));
    }

    @GetMapping("/{datasetId}/table")
    public ResponseEntity<ApiResponse<ResChartTableDto>> chartTable(@PathVariable String datasetId) {
        return ResponseEntity.ok(ApiResponse.ok(chartService.getChartTable(datasetId)));
    }
}