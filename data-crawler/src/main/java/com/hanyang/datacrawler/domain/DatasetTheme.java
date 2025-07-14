package com.hanyang.datacrawler.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
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