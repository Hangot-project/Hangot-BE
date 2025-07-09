package com.hanyang.dataportal.resource.repository;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.resource.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource,Long> {
    Optional<Resource> findByDataset(Dataset dataset);
    Optional<Resource> findById(Long resourceId);
}
