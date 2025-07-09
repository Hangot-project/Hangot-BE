package com.hanyang.dataportal.dataset.dto.req;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.vo.License;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import lombok.Data;

import java.util.List;

@Data
public class ReqDatasetDto {

    private String title;
    private String description;
    private Organization organization;
    private License license;
    private List<Theme> theme;
    public Dataset toEntity(){
        return Dataset.builder().
                title(title).
                description(description).
                organization(organization).
                license(license).
                scrap(0).
                download(0).
                build();
    }
}
