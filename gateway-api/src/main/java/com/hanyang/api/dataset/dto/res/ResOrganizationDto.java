package com.hanyang.api.dataset.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ResOrganizationDto {
    private List<String> organizationList;

    public ResOrganizationDto(List<String> organizationList) {
        this.organizationList = organizationList;
    }
}
