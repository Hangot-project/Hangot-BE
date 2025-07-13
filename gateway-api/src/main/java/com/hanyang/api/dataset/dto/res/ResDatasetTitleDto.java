package com.hanyang.api.dataset.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ResDatasetTitleDto {
    private List<String> titleList;

    public ResDatasetTitleDto(List<String> titleList) {
        this.titleList = titleList;
    }
}
