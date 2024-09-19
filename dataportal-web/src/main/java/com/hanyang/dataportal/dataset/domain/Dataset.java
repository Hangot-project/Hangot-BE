package com.hanyang.dataportal.dataset.domain;

import com.hanyang.dataportal.dataset.domain.vo.License;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.dto.req.ReqDatasetDto;
import com.hanyang.dataportal.resource.domain.Resource;
import com.hanyang.dataportal.user.domain.Download;
import com.hanyang.dataportal.user.domain.Scrap;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datasetId;
    private String title;
    @Lob
    private String description;
    @Enumerated(EnumType.STRING)
    private Organization organization;
    @Enumerated(EnumType.STRING)
    private License license;
    private LocalDate createdDate;
    private LocalDate updateDate;
    private Integer view;
    private Integer download;
    private Integer scrap;
    @OneToOne(mappedBy = "dataset")
    private Resource resource;
    @Builder.Default
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<DatasetTheme> datasetThemeList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Scrap> scrapList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Download> downloadList = new ArrayList<>();
    @PrePersist
    public void onPrePersist() {
        createdDate = LocalDate.now();
    }
    @PreUpdate
    public void onPreUpdate() {
        updateDate = LocalDate.now();
    }
    public void updateView() {
        if(this.view == null) this.view = 1;
        else this.view += 1;
    }
    public void updateScrap() {
        if(this.scrap == null) this.scrap = 1;
        else this.scrap += 1;
    }
    public void updateDataset(ReqDatasetDto reqDatasetDto){
       this.title = reqDatasetDto.getTitle();
       this.description = reqDatasetDto.getDescription();
       this.organization = reqDatasetDto.getOrganization();
    }
    public void addTheme(Theme theme){
        datasetThemeList.add(new DatasetTheme(this,theme));
    }
    public void removeTheme(){
        datasetThemeList.clear();
    }


}
