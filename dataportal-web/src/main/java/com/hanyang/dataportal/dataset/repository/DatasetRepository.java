package com.hanyang.dataportal.dataset.repository;

import com.hanyang.dataportal.dataset.domain.Dataset;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset,Long>{

    @Query("select d from Dataset d left join fetch d.resource left join fetch d.datasetThemeList where d.datasetId = :datasetId")
    Optional<Dataset> findByIdWithResourceAndTheme(Long datasetId);
    @Query("select d from Dataset d left join fetch d.datasetThemeList where d.datasetId = :datasetId")
    Optional<Dataset> findByIdWithTheme(Long datasetId);
    List<Dataset> findByTitleContaining(String keyword);
    @Query("select d from Dataset d left join d.resource order by d.popular DESC")
    List<Dataset> findOrderByPopular(Pageable pageable);
    @Query("select d from Dataset d  left join d.resource order by d.createdDate desc ")
    List<Dataset> findOrderByDateDesc(Pageable pageable);
}
