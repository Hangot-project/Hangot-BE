package com.hanyang.dataingestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResChartTableDto {
    private List<String> label;
    private List<List<String>> dataList;
}
