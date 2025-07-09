package com.hanyang.dataportal.dataset.service;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {

    public List<Organization> findByKeyword(String keyword){
        List<Organization> matchOrganization = new ArrayList<>();
        for(Organization organization:Organization.values()){
            if(organization.toString().contains(keyword)){
                matchOrganization.add(organization);
            }

        }
        return matchOrganization;
    }
}
