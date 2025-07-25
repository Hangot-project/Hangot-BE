package com.hanyang.api.dataset.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ResTypeListDto {
    private List<String> types;

    public ResTypeListDto(List<String> types) {
        this.types = types;
    }
}