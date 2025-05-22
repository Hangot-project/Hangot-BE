package com.hanyang.datastore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.datastore.core.exception.LabelNotFoundException;
import com.hanyang.datastore.core.exception.ResourceNotFoundException;
import com.hanyang.datastore.domain.MetaData;
import com.hanyang.datastore.domain.TableData;
import com.hanyang.datastore.dto.DatasetMetaDataDto;
import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.dto.ResChartTableDto;
import com.hanyang.datastore.infrastructure.S3StorageManager;
import com.hanyang.datastore.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@Service
@RequiredArgsConstructor
@Transactional
public class TableService {
    private final S3StorageManager s3StorageManager;
    private final DataPortalService dataPortalService;
    private final MetaDataRepository metaDataRepository;
    private final MongoTemplate mongoTemplate;
    /**
     * datasetId를 통해서
     * S3에서 데이터를 가져와서 MongoDB에 저장보
     * datasetId에 해당하는 리소스가 S3에 없거나 파싱 불가능한 파일형태(pdf,docx)면 예외처리
     */
    public void createDataTable(String datasetId) throws IOException {

        DatasetMetaDataDto datasetMetaDataDto = dataPortalService.findDataset(datasetId);

        InputStream file = s3StorageManager.getFile(datasetId);

        Workbook workbook = new XSSFWorkbook(file);
        DataFormatter dataFormatter = new DataFormatter();
        Sheet sheet = workbook.getSheetAt(0);

        Optional<MetaData> findMeta = metaDataRepository.findById(datasetId);
        MetaData metaData = findMeta
                .map(existing -> {
                    existing.updateDataset(datasetMetaDataDto);
                    existing.setDataListClean();
                    return existing;
                })
                .orElseGet(() -> new MetaData(datasetMetaDataDto));


        //col명 작성
        Row headerRow = sheet.getRow(0);
        String[] columns = StreamSupport.stream(headerRow.spliterator(), false)
                .map(dataFormatter::formatCellValue)
                .toArray(String[]::new);

        //data 추가
        List<TableData> tableDataList = new ArrayList<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                TableData tableData =new TableData();
                LinkedHashMap<String,Object> map = new LinkedHashMap<>();
                Iterator<Cell> cellIterator2 = row.iterator();
                for (int i=0;i<row.getPhysicalNumberOfCells();i++) {
                    Cell cell = cellIterator2.next();
                    Double cellValue = null;
                    try {
                        cellValue = Double.parseDouble(dataFormatter.formatCellValue(cell));
                    } catch (NumberFormatException e) {
                        map.put(columns[i], dataFormatter.formatCellValue(cell));
                        continue;
                    }
                    map.put(columns[i],cellValue);
                }
                if(row.getPhysicalNumberOfCells() != 0){
                    tableData.setData(map);
                    tableDataList.add(tableData);
                }
        }
        metaData.setDataList(tableDataList);

        metaDataRepository.save(metaData);
    }

    public ResChartDto getAggregationLabel(String datasetId, String colName,Integer type) throws JsonProcessingException {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(datasetId));
        query.fields().include("title").include("description").slice("dataList", 1);

        List<MetaData> metaData = mongoTemplate.find(query, MetaData.class, "metaData");
        //datasetId랑 일치하는 리소스가 없다
        if(metaData.isEmpty()) throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");

        MetaData meta = mongoTemplate.find(query, MetaData.class, "metaData").get(0);

        //aggregation 함수
        MatchOperation matchOperation = Aggregation.match(Criteria.where("_id").is(datasetId));
        UnwindOperation unwindOperation = unwind("dataList");
        GroupOperation groupOperation = Aggregation.group("data."+colName);
        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"));

        //순서보장을 위해 LinkedHashSet으로
        Set<Map.Entry<String, Object>> entries = new LinkedHashSet<>(meta.getDataList().get(0).getData().entrySet());

        //dataList의 크기 지정 및 dataName 설정
        //동적으로 groupOperation할 col을 지정해줌
        List<List<Double>> dataList = new ArrayList<>();
        List<String> dataName = new ArrayList<>();
        boolean isAxisExists = false;
        for (Map.Entry<String, Object> map :entries) {
            if(Objects.equals(map.getKey(), colName)) isAxisExists = true;
            if(!Objects.equals(map.getKey(), colName) && map.getValue() instanceof Double){
                if(type == 0) groupOperation = groupOperation.sum("data."+map.getKey()).as(map.getKey());
                else groupOperation = groupOperation.avg("data."+map.getKey()).as(map.getKey());

                dataList.add(new ArrayList<>());
                dataName.add(map.getKey());
            }
        }

        if(!isAxisExists) throw new LabelNotFoundException("해당 축은 존재하지 않습니다");

        //pipeLine 생성
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                project().andInclude("dataList").andExclude("_id"),
                unwindOperation,
                project().and("dataList.data").as("data"),
                groupOperation,
                sortOperation
        );

        //pipeLine을 통해 나온 필요한 값들 추출
        List<String> result = mongoTemplate.aggregate(aggregation, "metaData", String.class).getMappedResults();
        List<String> xLabel = new ArrayList<>();

        for (String str:result) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(str);

            //xLabel 값 . 있으면 제거
            String id = jsonNode.path("_id").asText();
            if(!Objects.equals(id, "null")) xLabel.add(id.replaceAll("\\..*", ""));


            int index = 0;
            for (Map.Entry<String, Object> map :entries) {
                if(!Objects.equals(map.getKey(), colName) && map.getValue() instanceof Double) {
                    double value = jsonNode.path(map.getKey()).asDouble();
                    dataList.get(index).add( Math.round(value * 100.0) / 100.0);
                    index++;
                }
            }

        }
        ResChartDto resChartDto = new ResChartDto();
        resChartDto.setX_axis_name(colName);
        resChartDto.setX_label(xLabel);
        resChartDto.setDataName(dataName);
        resChartDto.setDataList(dataList);
        return resChartDto;
    }

    public List<String> getAxis(String datasetId){

        //Meta data만 조회
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(datasetId));
        query.fields().slice("dataList", 1);;

        List<MetaData> metaData = mongoTemplate.find(query, MetaData.class, "metaData");
        //datasetId랑 일치하는 리소스가 없다
        if(metaData.isEmpty()) throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");

        MetaData meta = metaData.get(0);

        //순서보장을 위해 LinkedHashSet으로
        Set<Map.Entry<String, Object>> entries = new LinkedHashSet<>(meta.getDataList().get(0).getData().entrySet());

        //dataList의 크기 지정 및 dataName 설정
        //동적으로 groupOperation할 col을 지정해줌
        List<String> dataName = new ArrayList<>();
        for (Map.Entry<String, Object> map :entries) dataName.add(map.getKey());


        return dataName;

    }

    public ResChartTableDto getChartTable(String datasetId){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(datasetId));

        List<MetaData> metaData = mongoTemplate.find(query, MetaData.class, "metaData");
        //datasetId랑 일치하는 리소스가 없다
        if(metaData.isEmpty()){
            throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");
        }
        MetaData meta = metaData.get(0);
        Set<Map.Entry<String, Object>> entries = new LinkedHashSet<>(meta.getDataList().get(0).getData().entrySet());

        ArrayList<String> labelList = new ArrayList<>();
        for (Map.Entry<String, Object> map :entries) {
            labelList.add(map.getKey());
        }

        List<List<String>> dataList = new ArrayList<>();
        for(TableData tableData:meta.getDataList()){
            List<String> data = new ArrayList<>();
            for (Map.Entry<String, Object> map :tableData.getData().entrySet()) {
                data.add(removeTrailingPointZero(map.getValue().toString()));
            }
            dataList.add(data);
        }
        return new ResChartTableDto(labelList,dataList);
    }

    private String removeTrailingPointZero(String str) {
        if (str != null && str.matches("\\d+\\.0$")) {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }

}


