package com.hanyang.api.dataset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class DatasetTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datasetThemeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;
    
    private String theme;

}