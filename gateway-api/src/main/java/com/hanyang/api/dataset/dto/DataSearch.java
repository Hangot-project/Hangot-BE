package com.hanyang.api.dataset.dto;

import com.hanyang.api.dataset.utill.DatasetSort;
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
    private List<String> organization;
    private List<String> tag;
    private List<String> type;
    private DatasetSort sort;
    private int page;

}
