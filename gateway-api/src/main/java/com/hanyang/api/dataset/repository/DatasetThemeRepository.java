package com.hanyang.api.dataset.repository;

import com.hanyang.api.dataset.domain.DatasetTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetThemeRepository extends JpaRepository<DatasetTheme,Long> {
}
