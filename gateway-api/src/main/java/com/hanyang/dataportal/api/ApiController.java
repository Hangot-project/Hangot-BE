package com.hanyang.dataportal.api;

import com.hanyang.dataportal.api.dto.ResTitleDescDto;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.service.DatasetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "데이터스토어 API")
@RestController
@RequiredArgsConstructor
public class ApiController {

    private final DatasetService datasetService;

    @GetMapping("/dataset")
    public ResponseEntity<ResTitleDescDto> getDatasetMetaData(@RequestParam Long datasetId){
        Dataset dataset = datasetService.findById(datasetId);
        return ResponseEntity.ok(new ResTitleDescDto(dataset.getDatasetId(),dataset.getTitle(), dataset.getDescription()));
    }
}
