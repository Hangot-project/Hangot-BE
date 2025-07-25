package com.hanyang.api.dataset.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ResOrganizationListDto {
    private List<String> organizations;

    public ResOrganizationListDto(List<String> organizations) {
        this.organizations = organizations;
    }
}