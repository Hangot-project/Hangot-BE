package com.hanyang.dataportal.dataset.service;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class OrganizationServiceTest {

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService();
    }

    @Test
    @DisplayName("키워드에 따른 조직을 찾을 수 있다")
    void findByKeyword() {
        //Given
        final String keyword ="소프트";

        //When
        List<Organization> organizationList = organizationService.findByKeyword(keyword);

        //Then
        Assertions.assertThat(organizationList.size()).isEqualTo(1);
        Assertions.assertThat(organizationList.get(0)).isEqualTo(Organization.소프트융합대학);
    }
}