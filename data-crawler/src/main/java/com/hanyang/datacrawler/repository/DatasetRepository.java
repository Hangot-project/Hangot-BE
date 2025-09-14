package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    Optional<Dataset> findBySourceUrl(String sourceUrl);
}