package com.hanyang.api.dataOffer.dto;


import com.hanyang.api.dataOffer.domain.DataOffer;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReqDataOfferDto {

    private String name;  // 성명
    private LocalDate birthDay;  // 생년월일
    private String phoneNumber;  // 전화번호
    private String email;  // 이메일주소


    private String requestTitle;  // 신청내용
    private String dataName;  // 공공데이터명
    private String organizationName;  // 기관명
    private String dataContent;  // 공공데이터내용
    private String purpose;  // 활용목적
    private String purposeContent;  // 데이터활용목적

    private LocalDate localDate;


    public DataOffer toEntity() {
        return DataOffer.builder()
                .name(name)
                .email(email)
                .requestContent(requestTitle)
                .dataName(dataName)
                .organizationName(organizationName)
                .dataContent(dataContent)
                .purpose(purpose)
                .purposeContent(purposeContent)
                .date(localDate)
                .build();
    }

}


