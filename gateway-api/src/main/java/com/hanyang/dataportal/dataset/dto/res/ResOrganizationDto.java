package com.hanyang.dataportal.dataset.dto.res;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import lombok.Data;

import java.util.List;

@Data
public class ResOrganizationDto {
    private List<Organization> organizationList;

    public ResOrganizationDto(List<Organization> organizationList) {
        this.organizationList = organizationList;
    }
}
