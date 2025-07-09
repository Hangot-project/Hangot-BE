package com.hanyang.dataportal.dataset.dto.res;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import lombok.Data;

import java.util.List;

@Data
public class ResThemeListDto {
    private List<Theme> themeList;

    public ResThemeListDto(List<Theme> themeList) {
        this.themeList = themeList;
    }

}
