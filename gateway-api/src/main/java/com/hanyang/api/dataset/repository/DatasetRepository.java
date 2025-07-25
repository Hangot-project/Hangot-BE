package com.hanyang.api.dataset.repository;

import com.hanyang.api.dataset.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset,Long>{
    
    @Query("SELECT DISTINCT d.organization FROM Dataset d WHERE d.organization IS NOT NULL")
    List<String> findDistinctOrganizations();
    
    @Query("SELECT DISTINCT d.type FROM Dataset d WHERE d.type IS NOT NULL")
    List<String> findDistinctTypes();
}
