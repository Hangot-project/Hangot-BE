package com.hanyang.datastore.controller;

import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.infrastructure.GroupType;
import com.hanyang.datastore.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DataStoreControllerTest {

    @Mock
    private TableService tableService;

    @InjectMocks
    private DataStoreController dataStoreController;

    private ResChartDto mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = ResChartDto.builder()
                .x_axis_name("test-column")
                .x_label(List.of("A", "B", "C"))
                .dataName(List.of("data1"))
                .dataList(List.of(List.of(1.0, 2.0, 3.0)))
                .build();
    }

    @Test
    @DisplayName("Controller가 문자열 'SUM'을 GroupType.SUM enum으로 올바르게 변환하는지 테스트")
    void testEnumParameterBinding_SUM() {
        // Given
        String datasetId = "test-dataset";
        String colName = "test-column";
        GroupType groupType = GroupType.SUM; // Spring이 "SUM" 문자열을 이 enum으로 변환해야 함

        when(tableService.getAggregationLabel(eq(datasetId), eq(colName), eq(GroupType.SUM)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = dataStoreController.chart(datasetId, colName, groupType);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        verify(tableService).getAggregationLabel(datasetId, colName, GroupType.SUM);
    }

    @Test
    @DisplayName("Controller가 문자열 'AVG'를 GroupType.AVG enum으로 올바르게 변환하는지 테스트")
    void testEnumParameterBinding_AVG() {
        // Given
        String datasetId = "test-dataset";
        String colName = "test-column";
        GroupType groupType = GroupType.AVG;

        when(tableService.getAggregationLabel(eq(datasetId), eq(colName), eq(GroupType.AVG)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = dataStoreController.chart(datasetId, colName, groupType);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        verify(tableService).getAggregationLabel(datasetId, colName, GroupType.AVG);
    }
}
