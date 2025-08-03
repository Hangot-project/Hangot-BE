package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    Optional<Dataset> findBySourceUrl(String sourceUrl);
    
    @Query("SELECT DISTINCT d.title FROM Dataset d WHERE d.title IS NOT NULL AND d.title <> ''")
    List<String> findAllDistinctTitles();
}