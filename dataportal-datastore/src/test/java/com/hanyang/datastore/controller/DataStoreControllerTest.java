package com.hanyang.datastore.controller;

import com.hanyang.datastore.config.TestSecurityConfig;
import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.dto.ResChartTableDto;
import com.hanyang.datastore.infrastructure.GroupType;
import com.hanyang.datastore.service.TableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataStoreController.class)
@ActiveProfiles("test")
@WithMockUser
@Import(TestSecurityConfig.class)
class DataStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableService tableService;

    @Test
    void chart_성공() throws Exception {
        // given
        String datasetId = "test-dataset";
        String colName = "amount";
        GroupType groupType = GroupType.SUM;

        ResChartDto mockResponse = new ResChartDto();

        when(tableService.getAggregationLabel(datasetId, colName, groupType))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/dataset/{datasetId}/chart", datasetId)
                        .param("colName", colName)
                        .param("groupType", groupType.name()))
                .andExpect(status().isOk());
    }

    @Test
    void chart_기본값_테스트() throws Exception {
        // given
        String datasetId = "test-dataset";
        String colName = "amount";

        ResChartDto mockResponse = new ResChartDto();
        when(tableService.getAggregationLabel(datasetId, colName, GroupType.SUM))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/dataset/{datasetId}/chart", datasetId)
                        .param("colName", colName))
                .andExpect(status().isOk());
    }

    @Test
    void chartAxis_성공() throws Exception {
        // given
        String datasetId = "test-dataset";
        Set<String> mockAxis = Set.of("column1", "column2", "column3");

        when(tableService.getAxis(datasetId)).thenReturn(mockAxis);

        // when & then
        mockMvc.perform(get("/api/dataset/{datasetId}/axis", datasetId))
                .andExpect(status().isOk());
    }

    @Test
    void chartTable_성공() throws Exception {
        // given
        String datasetId = "test-dataset";
        ResChartTableDto mockResponse = new ResChartTableDto();

        when(tableService.getChartTable(datasetId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/dataset/{datasetId}/chart/table", datasetId))
                .andExpect(status().isOk());
    }

    @Test
    void chart_파라미터_누락하면_기본값으로() throws Exception {
        // given
        String datasetId = "test-dataset";

        // when & then
        mockMvc.perform(get("/api/dataset/{datasetId}/chart", datasetId))
                .andExpect(status().isOk());
    }

    @Test
    void chart_모든_GroupType_테스트() throws Exception {
        // given
        String datasetId = "test-dataset";
        String colName = "amount";

        for (GroupType groupType : GroupType.values()) {
            ResChartDto mockResponse = new ResChartDto();

            when(tableService.getAggregationLabel(datasetId, colName, groupType))
                    .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/dataset/{datasetId}/chart", datasetId)
                            .param("colName", colName)
                            .param("groupType", groupType.name()))
                    .andExpect(status().isOk());
        }
    }
}
