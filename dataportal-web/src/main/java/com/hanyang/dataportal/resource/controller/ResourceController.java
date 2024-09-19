package com.hanyang.dataportal.resource.controller;

import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.resource.dto.ResDataset;
import com.hanyang.dataportal.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.hanyang.dataportal.core.response.ApiResponse.ok;

@Tag(name = "파일데이터 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;
    @Operation(summary = "파일 데이터 저장 및 수정")
    @PutMapping(value = "/dataset/{datasetId}/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveResource(@PathVariable Long datasetId,
                                          @RequestPart(value = "file") MultipartFile multipartFile) {
        resourceService.save(datasetId, multipartFile);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "유저 리소스 다운로드")
    @PostMapping(value = "/download/{datasetId}")
    public ResponseEntity<?> downloadResource(@AuthenticationPrincipal UserDetails userDetail, @PathVariable Long datasetId) {
        resourceService.download(userDetail, datasetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "My 다운로드 리스트")
    @GetMapping("/my/downloads")
    public ResponseEntity<ApiResponse<List<ResDataset>>> getMyList(@AuthenticationPrincipal UserDetails userDetails) {
        List<Dataset> dataList = resourceService.getMyDownloadsList(userDetails.getUsername());
        return ResponseEntity.ok(ok(dataList.stream().map(ResDataset::new).toList()));

    }
}



