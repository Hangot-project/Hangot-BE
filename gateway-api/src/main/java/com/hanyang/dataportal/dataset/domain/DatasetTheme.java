package com.hanyang.dataportal.dataset.domain;

import com.hanyang.dataportal.dataset.domain.vo.Theme;
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
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="datasetId")
    private Dataset dataset;
    @Enumerated(EnumType.STRING)
    private Theme theme;

    public DatasetTheme(Dataset dataset,Theme theme) {
        this.theme = theme;
        this.dataset = dataset;
    }
}

