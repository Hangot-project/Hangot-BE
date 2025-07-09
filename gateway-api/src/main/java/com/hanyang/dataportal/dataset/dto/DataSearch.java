package com.hanyang.dataportal.dataset.dto;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import com.hanyang.dataportal.dataset.utill.DatasetSort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataSearch {
    private String keyword;
    private List<Organization> organization;
    private List<Theme> theme;
    private List<Type> type;
    private DatasetSort sort;
    private int page;

}
