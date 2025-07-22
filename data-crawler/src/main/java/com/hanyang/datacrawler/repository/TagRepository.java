package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TagRepository extends JpaRepository<Tag,Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Tag t WHERE t.dataset = :dataset")
    void deleteByDatasetOptimized(Dataset dataset);
}
