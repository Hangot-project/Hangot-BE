package com.hanyang.datastore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanyang.datastore.core.response.ApiResponse;
import com.hanyang.datastore.core.response.ResponseMessage;
import com.hanyang.datastore.dto.ResAxisDto;
import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.dto.ResChartTableDto;
import com.hanyang.datastore.infrastructure.GroupType;
import com.hanyang.datastore.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "데이터 스토어 API")
public class DataStoreController {

    private final TableService tableService;

    @Operation(summary = "파일 데이터 테이블화")
    @PostMapping("/dataset/{datasetId}/resource/table")
    public ResponseEntity<ApiResponse<?>> datastore(@PathVariable String datasetId) throws Exception {
        tableService.createDataTable(datasetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "파일 데이터 시각화 차트 데이터")
    @GetMapping("/dataset/{datasetId}/chart")
    public ResponseEntity<ApiResponse<ResChartDto>> chart(@PathVariable String datasetId, @RequestParam String colName){
        GroupType groupType = GroupType.fromCode(0);
        return ResponseEntity.ok(ApiResponse.ok(tableService.getAggregationLabel(datasetId,colName,groupType)));
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

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(ResponseMessage.NOT_EXIST_RESOURCE));
    }


}
