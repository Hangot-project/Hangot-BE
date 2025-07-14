package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithThemeDto;
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
import java.util.List;
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

    public DatasetWithThemeDto parseDatasetDetailPage(String html, String sourceURL) {
        try {
            Document doc = Jsoup.parse(html);

            String title = extractTitle(doc);
            Elements metaTable = doc.select(".file-meta-table-pc");

            String description = extractFromMetaTable(metaTable, "설명");
            String organization = extractFromMetaTable(metaTable, "제공기관");
            String createdDate = extractFromMetaTable(metaTable, "등록일");
            String updatedDate = extractFromMetaTable(metaTable, "수정일");
            String licenseStr = extractFromMetaTable(metaTable,"이용허락범위");
            String resourceName = extractFromMetaTable(metaTable, "파일데이터명");
            String type = extractFromMetaTable(metaTable, "확장자");
            List<String> themeList = Arrays.asList(extractFromMetaTable(metaTable, "키워드").split(","));

            Dataset dataset = Dataset.builder()
                    .title(title)
                    .description(description)
                    .organization(organization)
                    .createdDate(parseDate(createdDate))
                    .updatedDate(parseDate(updatedDate))
                    .license(licenseStr)
                    .type(type)
                    .resourceName(resourceName)
                    .sourceURL(sourceURL)
                    .build();

            return new DatasetWithThemeDto(dataset, themeList);
        } catch (Exception e) {
            log.error("상세 페이지 파싱 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("데이터셋 상세 페이지 파싱 실패", e);
        }
    }



    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("div.tit-area p.tit");
        return titleElement != null ? titleElement.text() : "";
    }

    private String extractFromMetaTable(Elements tables, String rowTitle) {
        for (Element table : tables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text();
                if (rowText.contains(rowTitle)) {
                    Elements cells = row.select("th, td");
                    for (int i = 0; i < cells.size() - 1; i++) {
                        String cellText = cells.get(i).text().trim();
                        if (cellText.contains(rowTitle)) {
                            return cleanExtractedValue(cells.get(i + 1).text().trim());
                        }
                    }
                }
            }
        }
        return "";
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
        if (dateStr == null || dateStr.isEmpty()) return LocalDate.now();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return LocalDate.now();
        }
    }
}
