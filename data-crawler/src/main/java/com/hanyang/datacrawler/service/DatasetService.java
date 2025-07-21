package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.domain.Tag;
import com.hanyang.datacrawler.repository.DatasetRepository;
import com.hanyang.datacrawler.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final TagRepository tagRepository;

    @Transactional
    public Dataset saveDatasetWithTag(Dataset dataset, List<String> tags) {
        log.debug("데이터셋 저장/수정 시도 - 제목: {}, 기관: {}", dataset.getTitle(), dataset.getOrganization());
        Optional<Dataset> existingDataset = datasetRepository.findBySourceUrl(dataset.getSourceUrl());
        
        Dataset savedDataset;
        if (existingDataset.isPresent()) {
            Dataset existing = existingDataset.get();
            existing.setTitle(dataset.getTitle());
            existing.setDescription(dataset.getDescription());
            existing.setOrganization(dataset.getOrganization());
            existing.setLicense(dataset.getLicense());
            existing.setCreatedDate(dataset.getCreatedDate());
            existing.setUpdatedDate(dataset.getUpdatedDate());
            existing.setResourceName(dataset.getResourceName());
            existing.setResourceUrl(dataset.getResourceUrl());
            existing.setType(dataset.getType());
            existing.setSource(dataset.getSource());
            
            savedDataset = datasetRepository.save(existing);
            log.info("기존 데이터셋 수정 완료 - ID: {}, 제목: {}, 출처: {}", savedDataset.getDatasetId(), savedDataset.getTitle(), savedDataset.getSourceUrl());
            
            tagRepository.deleteByDataset(savedDataset);
        } else {
            savedDataset = datasetRepository.save(dataset);
            log.info("새 데이터셋 저장 완료 - ID: {}, 제목: {}, 출처: {}", savedDataset.getDatasetId(), savedDataset.getTitle(), savedDataset.getSourceUrl());
        }

        if (tags != null && !tags.isEmpty()) {
            int savedCount = 0;
            for (String tag : tags) {
                tagRepository.save(Tag.builder()
                        .dataset(savedDataset)
                        .tag(tag.trim())
                        .build());
                savedCount++;
                log.debug("테마 저장 완료 - 데이터셋 ID: {}, 테마: {}", savedDataset.getDatasetId(), tag);
            }
            log.info("데이터셋 테마 리스트 저장 완료 - 데이터셋 ID: {}, 저장된 테마 수: {}", savedDataset.getDatasetId(), savedCount);
        }

        return savedDataset;
    }


    @Transactional
    public Dataset updateResourceUrl(Dataset dataset, String resourceUrl) {
        log.info("데이터셋 리소스 URL 업데이트 - ID: {}, URL: {}", dataset.getDatasetId(), resourceUrl);
        dataset.setResourceUrl(resourceUrl);
        return datasetRepository.save(dataset);
    }

}