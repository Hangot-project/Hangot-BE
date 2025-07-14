package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.DatasetTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetThemeRepository extends JpaRepository<DatasetTheme, Long> {
    @Modifying
    @Query("DELETE FROM DatasetTheme dt WHERE dt.dataset.datasetId = :datasetId")
    void deleteByDataset_DatasetId(@Param("datasetId") Long datasetId);
}