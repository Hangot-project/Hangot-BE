package com.hanyang.api.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ResAxisDto {
    Set<String> axis;
}