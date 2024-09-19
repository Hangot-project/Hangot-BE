package com.hanyang.dataportal.dataset.service;

import com.hanyang.dataportal.core.exception.ResourceNotFoundException;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.dto.DataSearch;
import com.hanyang.dataportal.dataset.dto.req.ReqDataSearchDto;
import com.hanyang.dataportal.dataset.dto.req.ReqDatasetDto;
import com.hanyang.dataportal.dataset.dto.res.ResDatasetDetailDto;
import com.hanyang.dataportal.dataset.repository.DatasetRepository;
import com.hanyang.dataportal.dataset.repository.DatasetSearchRepository;
import com.hanyang.dataportal.dataset.repository.DatasetThemeRepository;
import com.hanyang.dataportal.user.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DatasetService {
    private final DatasetRepository datasetRepository;
    private final DatasetSearchRepository datasetSearchRepository;
    private final DatasetThemeRepository datasetThemeRepository;
    private final ScrapRepository scrapRepository;

    public Dataset create(ReqDatasetDto reqDatasetDto) {
        Dataset dataset = reqDatasetDto.toEntity();

        List<Theme> themeList = reqDatasetDto.getTheme();
        for (Theme theme : themeList) {
            dataset.addTheme(theme);
        }
        return datasetRepository.save(dataset);
    }

    public Dataset update(Long datasetId, ReqDatasetDto reqDatasetDto) {
        Dataset dataset = datasetRepository.findByIdWithTheme(datasetId).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
        dataset.updateDataset(reqDatasetDto);
        datasetThemeRepository.deleteAll(dataset.getDatasetThemeList());
        dataset.removeTheme();

        List<Theme> themeList = reqDatasetDto.getTheme();
        for (Theme theme : themeList) {
            dataset.addTheme(theme);
        }

        return dataset;
    }

    @Transactional(readOnly = true)
    public Page<Dataset> getDatasetList(ReqDataSearchDto reqDataSearchDto) {

        DataSearch dataSearch = DataSearch.builder().keyword(reqDataSearchDto.getKeyword()).
                organization(reqDataSearchDto.getOrganization()).
                theme(reqDataSearchDto.getTheme()).
                page(reqDataSearchDto.getPage()).
                type(reqDataSearchDto.getType()).
                sort(reqDataSearchDto.getSort()).
                build();
        return datasetSearchRepository.searchDatasetList(dataSearch);

    }

    public ResDatasetDetailDto getDatasetDetail(Long datasetId) {
        Dataset dataset = datasetRepository.findByIdWithResourceAndTheme(datasetId).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
        Long scrapCount = scrapRepository.countByDataset(dataset);
        dataset.updateView();
        return new ResDatasetDetailDto(dataset, scrapCount.intValue());
    }

    public Dataset findById(Long id) {
        return datasetRepository.findByIdWithTheme(id).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
    }

    public void delete(Long id) {
        Dataset dataset = findById(id);
        datasetRepository.delete(dataset);
    }

    @Transactional(readOnly = true)
    public List<String> getByKeyword(String keyword) {
        return datasetRepository.findByTitleContaining(keyword).stream().map(Dataset::getTitle).toList();
    }

    public List<Dataset> getPopular() {
        Pageable pageable = PageRequest.of(0, 6);
        return datasetRepository.findOrderByPopular(pageable);
    }

    public List<Dataset> getNew() {
        Pageable pageable = PageRequest.of(0, 6);
        return datasetRepository.findOrderByDateDesc(pageable);
    }
}
