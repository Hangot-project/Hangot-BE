package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag,Long> {
    void deleteByDataset(Dataset dataset);
}
