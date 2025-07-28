package com.hanyang.api.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResPieChartDto {
    private String axis_name;
    private List<String> labels;
    private List<Long> count;
}