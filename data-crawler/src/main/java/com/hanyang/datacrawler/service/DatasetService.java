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
        log.debug("데이터셋 저장 시도 - 제목: {}, 기관: {}", dataset.getTitle(), dataset.getOrganization());
        Optional<Dataset> existingDataset = datasetRepository.findByTitleAndOrganization(
                dataset.getTitle(), dataset.getOrganization());

        Dataset savedDataset = existingDataset.orElseGet(() -> {
            Dataset d = datasetRepository.save(dataset);
            log.info("새 데이터셋 저장 완료 - ID: {}, 제목: {}, 기관: {}",
                    d.getDatasetId(), d.getTitle(), d.getOrganization());
            return d;
        });

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