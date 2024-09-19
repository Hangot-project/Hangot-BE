package com.hanyang.dataportal.dataOffer.dto;

import com.hanyang.dataportal.dataOffer.domain.DataOffer;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
@Data
public class ResDataOfferListDto {
    private Integer totalPage;
    private Long totalElement;
    private List<SimpleDataOffer> data;
    public ResDataOfferListDto(Page<DataOffer> dataOffer){
        this.totalPage = dataOffer.getTotalPages();
        this.totalElement = dataOffer.getTotalElements();
        this.data = dataOffer.getContent().stream().map(SimpleDataOffer::new).toList();
    }

    @Data
    public static class SimpleDataOffer{
        private Long id;
        private String title;
        private LocalDate createDate;
        public SimpleDataOffer(DataOffer dataOffer) {
            this.id = dataOffer.getId();
            this.title = dataOffer.getDataContent();
            this.createDate = dataOffer.getDate();
        }
    }
}
