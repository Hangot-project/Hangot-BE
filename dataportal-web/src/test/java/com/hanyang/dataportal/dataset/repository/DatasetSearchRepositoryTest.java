package com.hanyang.dataportal.dataset.repository;

import com.hanyang.dataportal.TestQueryDSLConfig;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.dto.DataSearch;
import com.hanyang.dataportal.dataset.utill.DatasetSort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ActiveProfiles("test")
@DataJpaTest
@Import(TestQueryDSLConfig.class)
class DatasetSearchRepositoryTest {
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private DatasetSearchRepository datasetSearchRepository;
    @BeforeEach
    void setUp() {
        List<Dataset> datasetList = new ArrayList<>();
        for (int i = 2000; i < 2021; i++) {
            Dataset.DatasetBuilder da = Dataset.builder().
                    title("대학교 " + i + "년도 입학생").
                    view(1000-i);
            if(i<2013){
                da.organization(Organization.소프트융합대학);
            }
            else{
                da.organization(Organization.공과대학);
            }

            Dataset dataset = da.build();

            dataset.addTheme(Theme.입학);
            if(i<2010){
                dataset.addTheme(Theme.장학);
            }
            else{
                dataset.addTheme(Theme.재정);
            }

            datasetList.add(dataset);
        }
        datasetRepository.saveAll(datasetList);
    }

    @DisplayName("10개씩 페이징해서 데이터셋을 가져올 수 있다.")
    @Test
    void searchDatasetListPaging() {
        //given
        DataSearch datasearch = new DataSearch();
        datasearch.setPage(0);
        datasearch.setSort(DatasetSort.최신);
        //when
        Page<Dataset> datasets = datasetSearchRepository.searchDatasetList(datasearch);

        //then
        Assertions.assertThat(datasets.getTotalElements()).isEqualTo(21);
        Assertions.assertThat(datasets.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(datasets.getContent().size()).isEqualTo(10);
    }

    //TODO 테스트 오류,swagger는 잘됨
    @DisplayName("키워드 검색을 통해 데이터셋을 찾을 수 있다.")
    @Test
    void searchDatasetListKeyWord() {
        //given
        DataSearch dataSearch = new DataSearch();
        dataSearch.setPage(0);
        dataSearch.setKeyword("201");
        dataSearch.setSort(DatasetSort.최신);
        //when
        Page<Dataset> datasets = datasetSearchRepository.searchDatasetList(dataSearch);

        //then
        Assertions.assertThat(datasets.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(datasets.getTotalPages()).isEqualTo(1);
    }

    @DisplayName("여러 제공기관에 해당하는 데이터셋을 찾을 수 있다.")
    @Test
    void searchDatasetListOrganization() {
        //given
        DataSearch dataSearch = new com.hanyang.dataportal.dataset.dto.DataSearch();
        dataSearch.setPage(0);
        dataSearch.setOrganization(Stream.of(Organization.공과대학).collect(Collectors.toList()));
        dataSearch.setSort(DatasetSort.최신);

        //when
        Page<Dataset> datasets = datasetSearchRepository.searchDatasetList(dataSearch);

        //then
        Assertions.assertThat(datasets.getTotalElements()).isEqualTo(8);
        Assertions.assertThat(datasets.getTotalPages()).isEqualTo(1);
    }

    @DisplayName("여러 주제에 해당하는 데이터셋을 찾을 수 있다.")
    @Test
    void searchDatasetListTheme() {
        //given

        DataSearch dataSearch = new com.hanyang.dataportal.dataset.dto.DataSearch();
        dataSearch.setPage(0);
        dataSearch.setTheme(Collections.singletonList(Theme.입학));
        dataSearch.setSort(DatasetSort.최신);

        //when
        Page<Dataset> datasets = datasetSearchRepository.searchDatasetList(dataSearch);

        //then
        Assertions.assertThat(datasets.getTotalElements()).isEqualTo(21);
        Assertions.assertThat(datasets.getTotalPages()).isEqualTo(3);
    }

    @DisplayName("조회순으로 데이터셋을 정렬할 수 있다.")
    @Test
    void searchDatasetListSort() {
        //given
        DataSearch datasearch = new DataSearch();
        datasearch.setPage(0);
        datasearch.setSort(DatasetSort.조회);

        //when
        Page<Dataset> datasets = datasetSearchRepository.searchDatasetList(datasearch);

        //then
        Assertions.assertThat(datasets.getTotalElements()).isEqualTo(21);
        Assertions.assertThat(datasets.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(datasets.getContent().get(0).getView()).isGreaterThan(datasets.getContent().get(1).getView());
    }
}