package com.hanyang.datacrawler.dto;

import com.hanyang.datacrawler.domain.Dataset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DatasetWithTag {
    private Dataset dataset;
    private List<String> tags;
}
