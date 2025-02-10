package com.hanyang.dataportal.resource.domain;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;
    @Lob
    private String resourceUrl;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String resourceName;
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasetId")
    private Dataset dataset;

    public void updateResource(String url, Type type,String fileName) {
        this.resourceUrl = url;
        this.type = type;
        this.resourceName = fileName;
    }

}
