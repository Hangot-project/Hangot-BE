package com.hanyang.api.dataset.dto.req;

import com.hanyang.api.dataset.utill.DatasetSort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqDataSearchDto {
    private String keyword;
    private List<String> organizations;
    private List<String> tags;
    private List<String> types;
    private DatasetSort sort = DatasetSort.최신;
    private int page = 1;
}
