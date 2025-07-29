package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class DataGoKrHtmlParser {

    public Optional<LocalDate> getDatasetDate(Element element) {
        Element ele = element.select(".tit:contains(수정일)").last();
        if(ele == null) return Optional.empty();

        Element date = ele.nextElementSibling();
        if(date == null) return Optional.empty();
        String dateText = date.text().trim();

        return Optional.ofNullable(parseDate(dateText));
    }


    public DatasetWithTag parseMetaData(String html, String sourceUrl) {
        Document doc = Jsoup.parse(html);

        String title = extractTitle(doc);
        Elements metaTable = doc.select(".file-meta-table-pc");

        Map<String, String> metaData = extractAllMetaData(metaTable);
        
        String description = metaData.getOrDefault("설명", "");
        String organization = metaData.getOrDefault("제공기관", "");
        String createdDate = metaData.getOrDefault("등록일", "");
        String updatedDate = metaData.getOrDefault("수정일", "");
        String licenseStr = metaData.getOrDefault("이용허락범위", "");
        String type = metaData.getOrDefault("확장자", "");
        String fileName = metaData.getOrDefault("파일데이터명", "");
        String resourceName = fileName.isEmpty() ? "" : fileName + "." + type.toLowerCase();
        String keywords = metaData.getOrDefault("키워드", "");
        List<String> tagList = keywords.isEmpty() ? new ArrayList<>() : Arrays.asList(keywords.split(","));

        LocalDate parsedUpdatedDate = parseDate(updatedDate);



        Dataset dataset = Dataset.builder()
                .title(title)
                .description(description)
                .organization(organization)
                .createdDate(parseDate(createdDate))
                .updatedDate(parsedUpdatedDate)
                .license(licenseStr)
                .type(type)
                .resourceName(resourceName)
                .sourceUrl(sourceUrl)
                .source("공공 데이터 포털")
                .build();

        return new DatasetWithTag(dataset, tagList);
    }



    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("div.tit-area p.tit");
        return titleElement != null ? titleElement.text() : "";
    }


    private Map<String, String> extractAllMetaData(Elements tables) {
        Map<String, String> metaData = new HashMap<>();
        
        for (Element table : tables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cells = row.select("th, td");
                for (int i = 0; i < cells.size() - 1; i++) {
                    String cellText = cells.get(i).text().trim();
                    String nextCellText = cells.get(i + 1).text().trim();
                    
                    if (!cellText.isEmpty() && !nextCellText.isEmpty()) {
                        metaData.put(cellText, cleanExtractedValue(nextCellText));
                    }
                }
            }
        }
        
        return metaData;
    }

    private String cleanExtractedValue(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        value = value.replaceAll("<[^>]+>", "");
        value = value.replaceAll("\\s+", " ");
        return value.trim();
    }

    private LocalDate parseDate(String dateStr) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (Exception e){
            return null;
        }

    }
}
