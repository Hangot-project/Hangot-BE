package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    
    Optional<Dataset> findByTitle(String title);
    
    Optional<Dataset> findByTitleAndOrganization(String title, String organization);
    
    List<Dataset> findByOrganization(String organization);
    
    @Query("SELECT d FROM Dataset d JOIN d.datasetThemeList dt WHERE dt.theme = :theme")
    List<Dataset> findByTheme(@Param("theme") String theme);
}