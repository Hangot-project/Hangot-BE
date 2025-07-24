package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import com.hanyang.datacrawler.exception.CrawlStopException;
import com.hanyang.datacrawler.exception.NoCrawlNextDayException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class DataGoKrHtmlParser {

    private static final String DATA_GO_KR_BASE_URL = "https://www.data.go.kr";


    public List<String> parseDatasetUrls(String html) {
        List<String> datasetUrls = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(html);
            log.debug("HTML 파싱 시작 - 전체 HTML 길이: {}", html.length());

            Elements datasetElements = doc.select("#fileDataList li");
            log.debug("찾은 데이터셋 요소 수: {}", datasetElements.size());

            for (Element element : datasetElements) {
                Optional<String> datasetUrl = parseDatasetUrl(element);
                datasetUrl.ifPresent(datasetUrls::add);
            }
        } catch (Exception e) {
            log.error("HTML 파싱 중 오류: {}", e.getMessage());
            log.debug("HTML 내용 일부: {}", html.substring(0, Math.min(500, html.length())));
        }

        return datasetUrls;
    }

    public Optional<LocalDate> getLastDatasetDateFromPage(String html) {
        try {
            Document doc = Jsoup.parse(html);
            Elements titles = doc.select(".tit:contains(수정일)");
            if (titles.isEmpty()) {
                return Optional.empty();
            }
            Element lastTitle = titles.last();

            Element next = lastTitle.nextElementSibling();
            if (next == null) {
                return Optional.empty();
            }

            String dateText = next.text().trim();
            return Optional.of(parseDate(dateText));
        } catch (Exception e) {
            log.error("페이지에서 마지막 데이터셋 날짜 추출 실패", e);
        }

        return Optional.empty();
    }



    private Optional<String> parseDatasetUrl(Element element) {
        try {
            Element linkElement = element.selectFirst("a[href*='/data/']");
            if (linkElement == null) return Optional.empty();
            String datasetUrl = linkElement.attr("href");
            if (datasetUrl.isEmpty()) return Optional.empty();
            String resourceUrl = datasetUrl.startsWith("/") ? DATA_GO_KR_BASE_URL + datasetUrl : datasetUrl;
            return Optional.of(resourceUrl);
        } catch (Exception e) {
            log.error("데이터셋 요소 파싱 중 오류: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }


    public DatasetWithTag parseMetaData(String html, String sourceUrl, LocalDate startDate, LocalDate endDate) {
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

        // endDate보다 최신 데이터는 스킵
        if (endDate != null && parsedUpdatedDate.isAfter(endDate)) {
            throw new NoCrawlNextDayException(title, parsedUpdatedDate, endDate);
        }

        // startDate보다 이전 데이터 만나면 크롤링 종료
        if (startDate != null && parsedUpdatedDate.isBefore(startDate)) {
            throw new CrawlStopException(title, parsedUpdatedDate, startDate);
        }

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateStr.trim(), formatter);
    }
}
