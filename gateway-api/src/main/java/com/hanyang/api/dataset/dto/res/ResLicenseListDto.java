package com.hanyang.api.dataset.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ResLicenseListDto {
    private List<String> licensesList;

    public ResLicenseListDto(List<String> licensesList) {
        this.licensesList = licensesList;
    }
}
