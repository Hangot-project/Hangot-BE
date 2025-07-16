package com.hanyang.api.user.dto;


import com.hanyang.api.user.domain.Scrap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResScrapDto {
    /**
     * scrap id
     */
    @Schema(description = "스크랩 객체의 id값", example = "1")
    private Long scrapId;
    /**
     * 스크랩한 dataset
     */
    @Schema(description = "스크랩한 데이터셋의 id값", example = "1")
    private Long datasetId;
    /**
     * 스크랩한 dataset의 제목
     */
    @Schema(description = "스크랩한 데이터셋의 제목", example = "2024 한양대학교 에리카 입학 경쟁률")
    private String title;
    /**
     * 스크랩한 dataset의 설명
     */
    @Schema(description = "스크랩한 데이터셋의 설명", example = "2024 한양대학교 에리카 입학 경쟁률에 대한 데이터 입니다.")
    private String description;
    /**
     * 스크랩한 dataset의 리소스 타입
     */
    @Schema(description = "스크랩한 데이터셋의 리소스 타입", example = "csv")
    private String type;
    /**
     * 스크랩한 dataset의 조직
     */
    @Schema(description = "스크랩한 데이터셋의 조직", example = "입학처")
    private String organization;

    public ResScrapDto(Scrap scrap) {
        this.scrapId = scrap.getScrapId();
        this.datasetId = scrap.getDataset().getDatasetId();
        this.title = scrap.getDataset().getTitle();
        this.description = scrap.getDataset().getDescription();
        this.type = scrap.getDataset().getType();
        this.organization = scrap.getDataset().getOrganization();
    }
}
