package com.hanyang.dataportal.dataset.service;

import com.hanyang.dataportal.core.exception.ResourceNotFoundException;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.DatasetTheme;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.dto.req.ReqDatasetDto;
import com.hanyang.dataportal.dataset.dto.res.ResDatasetDetailDto;
import com.hanyang.dataportal.dataset.repository.DatasetRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Sql("/h2-truncate.sql")
class DatasetServiceTest {

    @Autowired
    private DatasetService datasetService;
    @Autowired
    private DatasetRepository datasetRepository;


    @Test
    @DisplayName("데이터셋을 저장할 수 있다.")
    void create() {
        //Given
        ReqDatasetDto reqDatasetDto = new ReqDatasetDto();
        reqDatasetDto.setOrganization(Organization.소프트융합대학);
        reqDatasetDto.setTheme(new ArrayList<>(List.of(Theme.학생, Theme.입학)));

        // When
        Dataset savedDataset = datasetService.create(reqDatasetDto);

        // Then
        Assertions.assertThat(datasetRepository.findById(savedDataset.getDatasetId())).isPresent();
        Assertions.assertThat(savedDataset.getOrganization()).isEqualTo(Organization.소프트융합대학);
        Assertions.assertThat(
                savedDataset.getDatasetThemeList().stream().map(DatasetTheme::getTheme).toList())
                .isEqualTo(new ArrayList<>(List.of(Theme.학생, Theme.입학)));
    }

    @Test
    @DisplayName("데이터셋을 수정할 수 있다.")
    void update() {
        //Given
        Dataset dataset = datasetRepository.save(Dataset.builder().build());
        ReqDatasetDto reqDatasetDto = new ReqDatasetDto();
        reqDatasetDto.setOrganization(Organization.소프트융합대학);
        reqDatasetDto.setTheme(new ArrayList<>(List.of(Theme.학생, Theme.입학)));

        //When
        Dataset savedDataset = datasetService.update(dataset.getDatasetId(), reqDatasetDto);

        //Then
        Assertions.assertThat(savedDataset.getOrganization()).isEqualTo(Organization.소프트융합대학);
        Assertions.assertThat(savedDataset.getDatasetThemeList().stream().map(DatasetTheme::getTheme).toList())
                        .isEqualTo(new ArrayList<>(List.of(Theme.학생, Theme.입학)));
    }

    @Test
    @DisplayName("데이터셋을 조회하면 조회수가 1 증가 한다")
    void getDatasetViewPlus() {
        //Given
        Dataset dataset = datasetRepository.save(Dataset.builder().view(0).build());

        //When
        ResDatasetDetailDto findDataset = datasetService.getDatasetDetail(dataset.getDatasetId());

        //Then
        Assertions.assertThat(findDataset.getView()).isEqualTo(1);
    }

    @Test
    @DisplayName("데이터셋을 삭제할 수 있다")
    void delete() {
        //Given
        Dataset dataset = datasetRepository.save(Dataset.builder().build());

        //When
        datasetService.delete(dataset.getDatasetId());

        //Then
        Assertions.assertThat(datasetRepository.findById(dataset.getDatasetId())).isEmpty();
    }


    @Test
    @DisplayName("존재하지 않는 데이터를 삭제할 수 없다.")
    void deleteThrow() {
        //Given
        Long datasetId = 1L;

        //When & Then
        Assertions.assertThatThrownBy(()->{
                    datasetService.delete(datasetId);})
                .isExactlyInstanceOf(ResourceNotFoundException.class).hasMessage("해당 데이터셋은 존재하지 않습니다");
    }

    @Test
    @DisplayName("키워드에 따라 데이터셋 제목을 찾을 수 있다.")
    void getByKeywordTitle() {
        //Given
        String keyword = "입학";
        String title = "2023년도 입학생 수";
        Dataset dataset = Dataset.builder().title("2023년도 입학생 수").build();
        Dataset dataset2 = Dataset.builder().title("2023년도 취업률").build();
        datasetRepository.save(dataset);
        datasetRepository.save(dataset2);

        //When
        List<String> titleList = datasetService.getByKeyword(keyword);

        //Then
        Assertions.assertThat(titleList.size()).isEqualTo(1);
        Assertions.assertThat(titleList.get(0)).isEqualTo(title);

    }
}