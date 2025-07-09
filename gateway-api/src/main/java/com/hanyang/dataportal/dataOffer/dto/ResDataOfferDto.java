package com.hanyang.dataportal.dataOffer.dto;

import com.hanyang.dataportal.dataOffer.domain.DataOffer;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResDataOfferDto {
    private Long dataOfferId; // 요청 게시글번호

    private String requestTitle; // 요청제목
    private String dataName;  // 공공데이터명
    private String organizationName;  // 기관명
    private String purpose; // 목적(드롭탭)
    private String purposeContent; // 목적(컨텐츠)

    private LocalDate date; // 등록일자

    public ResDataOfferDto(DataOffer dataOffer) {
        this.dataOfferId = dataOffer.getId();

        this.dataName = dataOffer.getDataName();
        this.requestTitle = dataOffer.getRequestContent();
        this.organizationName = dataOffer.getOrganizationName();
        this.purpose = dataOffer.getPurpose();
        this.purposeContent = dataOffer.getPurposeContent();

        this.date = dataOffer.getDate();
    }

}
