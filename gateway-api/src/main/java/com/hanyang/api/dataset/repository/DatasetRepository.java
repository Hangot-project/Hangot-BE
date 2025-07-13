package com.hanyang.api.dataset.repository;

import com.hanyang.api.dataset.domain.Dataset;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset,Long>{

    @Query("select d from Dataset d where d.datasetId = :datasetId")
    Optional<Dataset> findByIdWithResourceAndTheme(@Param("datasetId") Long datasetId);
    @Query("select d from Dataset d where d.datasetId = :datasetId")
    Optional<Dataset> findByIdWithTheme(@Param("datasetId") Long datasetId);
    List<Dataset> findByTitleContaining(String keyword);
    @Query("select d from Dataset d order by d.popular DESC")
    List<Dataset> findOrderByPopular(Pageable pageable);
    @Query("select d from Dataset d  order by d.createdDate desc ")
    List<Dataset> findOrderByDateDesc(Pageable pageable);
}
