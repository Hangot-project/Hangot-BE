package com.hanyang.dataportal.dataset.repository;

import com.hanyang.dataportal.dataset.domain.DatasetTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetThemeRepository extends JpaRepository<DatasetTheme,Long> {
}
