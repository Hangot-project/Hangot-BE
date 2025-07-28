package com.hanyang.api.dataset.repository;

import com.hanyang.api.dataset.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset,Long>{
    
    @Query("SELECT DISTINCT d.organization FROM Dataset d WHERE d.organization IS NOT NULL AND d.organization <> ''")
    List<String> findDistinctOrganizations();

    @Query("SELECT DISTINCT d.type FROM Dataset d WHERE d.type IS NOT NULL AND d.type <> ''")
    List<String> findDistinctTypes();

    @Query("SELECT DISTINCT t.tag FROM Tag t WHERE t.tag IS NOT NULL AND t.tag <> '' AND t.tag LIKE :keyword% ORDER BY t.tag")
    List<String> findTagsContaining(String keyword);

    @Query("SELECT DISTINCT d.title FROM Dataset d WHERE d.title IS NOT NULL AND d.title <> '' AND REPLACE(REPLACE(d.title, ' ', ''), '_', '') LIKE %:keyword% ORDER BY d.title LIMIT 10")
    List<String> findTitlesContaining(String keyword);

}
