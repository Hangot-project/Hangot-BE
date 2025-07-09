package com.hanyang.dataportal.user.domain;

import com.hanyang.dataportal.dataset.domain.Dataset;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Download {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long downloadId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasetId")
    private Dataset dataset;
    public Long getDatasetId() {
        return dataset.getDatasetId();
    }

}