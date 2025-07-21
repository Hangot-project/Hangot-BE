package com.hanyang.datacrawler.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datasetId;
    private String title;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private String organization;
    private String license;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private String resourceName;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String resourceUrl;
    private String type;
    private String sourceUrl;
    private String source;
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Tag> tagList = new ArrayList<>();
}
