package com.hanyang.api.dataset.domain;

import com.hanyang.api.user.domain.Scrap;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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
    private Integer view;
    private Integer scrap;
    private String resourceName;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String resourceUrl;
    private String type;
    private String sourceUrl;
    private String source;

    @Formula(value = "view + 5 * scrap")
    private Integer popular;
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Tag> tagList = new ArrayList<>();
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Scrap> scrapList = new ArrayList<>();

    public void updateView() {
        if(this.view == null) this.view = 1;
        else this.view += 1;
    }
    public void updateScrap() {
        if(this.scrap == null) this.scrap = 1;
        else this.scrap += 1;
    }
}
