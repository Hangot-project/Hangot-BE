package com.hanyang.api.dataset.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ResThemeListDto {
    private List<String> themeList;

    public ResThemeListDto(List<String> themeList) {
        this.themeList = themeList;
    }

}
