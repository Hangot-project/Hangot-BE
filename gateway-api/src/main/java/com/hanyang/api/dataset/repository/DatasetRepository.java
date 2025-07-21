package com.hanyang.api.dataset.repository;

import com.hanyang.api.dataset.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset,Long>{
}
