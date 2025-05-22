package com.hanyang.datastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResChartDto {
    private String x_axis_name;
    private List<String> x_label;
    private List<String> dataName;
    private List<List<Double>> dataList;
}
