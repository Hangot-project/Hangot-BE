package com.hanyang.dataportal.dataset.domain;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class DataRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataRequestId;
    private String title;
    @Enumerated(EnumType.STRING)
    private Organization organization;
    @Lob
    private String content;
    private String purpose;
    
}
