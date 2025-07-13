package com.hanyang.api.dataset.dto.res;

import com.hanyang.api.dataset.domain.vo.License;
import lombok.Data;

import java.util.List;

@Data
public class ResLicenseListDto {
    private List<License> licensesList;

    public ResLicenseListDto(List<License> licensesList) {
        this.licensesList = licensesList;
    }
}
