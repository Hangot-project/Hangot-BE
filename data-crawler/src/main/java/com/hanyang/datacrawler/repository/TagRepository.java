package com.hanyang.datacrawler.repository;

import com.hanyang.datacrawler.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag,Long> {

    @Query("SELECT DISTINCT t.tag FROM Tag t WHERE t.tag IS NOT NULL AND t.tag <> ''")
    List<String> findAllDistinctTags();
}
