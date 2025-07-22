package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.domain.Tag;
import com.hanyang.datacrawler.repository.DatasetRepository;
import com.hanyang.datacrawler.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final TagRepository tagRepository;

    @Transactional
    public Dataset updateResourceUrl(Dataset dataset, String resourceUrl) {
        log.debug("리소스 URL 업데이트: {}", dataset.getTitle());
        dataset.setResourceUrl(resourceUrl);
        return datasetRepository.save(dataset);
    }

    @Transactional
    public List<Dataset> saveDatasetsBatch(List<Dataset> datasets, List<List<String>> tagsList) {
        if (datasets.size() != tagsList.size()) {
            throw new IllegalArgumentException("데이터셋과 태그 리스트 크기가 일치하지 않습니다.");
        }

        List<Dataset> savedDatasets = new ArrayList<>();
        List<Tag> allTags = new ArrayList<>();

        for (int i = 0; i < datasets.size(); i++) {
            Dataset dataset = datasets.get(i);
            List<String> tags = tagsList.get(i);

            Optional<Dataset> existingDataset = datasetRepository.findBySourceUrl(dataset.getSourceUrl());
            Dataset savedDataset;
            
            if (existingDataset.isPresent()) {
                Dataset existing = existingDataset.get();
                updateDatasetFields(existing, dataset);
                savedDataset = existing;
                tagRepository.deleteByDatasetOptimized(savedDataset);
            } else {
                savedDataset = dataset;
            }

            savedDatasets.add(savedDataset);

            if (tags != null && !tags.isEmpty()) {
                List<Tag> tagEntities = tags.stream()
                        .filter(tag -> tag != null && !tag.trim().isEmpty())
                        .map(tag -> Tag.builder()
                                .dataset(savedDataset)
                                .tag(tag.trim())
                                .build())
                        .toList();
                allTags.addAll(tagEntities);
            }
        }

        List<Dataset> result = datasetRepository.saveAll(savedDatasets);
        
        if (!allTags.isEmpty()) {
            tagRepository.saveAll(allTags);
            log.debug("배치 처리 완료: 데이터셋 {}개, 태그 {}개", result.size(), allTags.size());
        }

        return result;
    }

    private void updateDatasetFields(Dataset existing, Dataset newData) {
        existing.setTitle(newData.getTitle());
        existing.setDescription(newData.getDescription());
        existing.setOrganization(newData.getOrganization());
        existing.setLicense(newData.getLicense());
        existing.setCreatedDate(newData.getCreatedDate());
        existing.setUpdatedDate(newData.getUpdatedDate());
        existing.setResourceName(newData.getResourceName());
        existing.setResourceUrl(newData.getResourceUrl());
        existing.setType(newData.getType());
        existing.setSource(newData.getSource());
    }

}