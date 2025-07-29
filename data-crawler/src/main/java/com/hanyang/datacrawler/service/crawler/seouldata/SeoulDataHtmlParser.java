package com.hanyang.datacrawler.service.crawler.seouldata;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeoulDataHtmlParser {
    
    private final SeoulDataDownloadParamExtractor downloadParamExtractor;
    public Optional<DatasetWithTag> parseDatasetDetail(String html, String sourceUrl) {
        Document doc = Jsoup.parse(html);

        try {
            String title = doc.select("meta[property=og:title]").attr("content");
            String description = doc.select("meta[property=og:description]").attr("content");

            Elements tableRows = doc.select(".tbl-base-d tbody tr");
            String createdDate = "";
            String updatedDate = "";
            String organization = "";
            String license = "";
            List<String> tags = new ArrayList<>();

            for (Element row : tableRows) {
                Elements headers = row.select("th");
                Elements cells = row.select("td");

                for (int i = 0; i < headers.size() && i < cells.size(); i++) {
                    String headerText = headers.get(i).text().trim();
                    String cellText = cells.get(i).text().trim();

                    switch (headerText) {
                        case "공개일자":
                            createdDate = cellText;
                            break;
                        case "데이터 갱신일":
                            updatedDate = cellText;
                            break;
                        case "제공기관":
                            organization = cellText;
                            if (!cellText.isEmpty()) {
                                tags.add(cellText);
                            }
                            break;
                        case "라이선스":
                            license = cellText;
                            break;
                        case "관련 태그":
                            Elements tagLinks = cells.get(i).select("a");
                            for (Element tagLink : tagLinks) {
                                String tagText = tagLink.text().trim();
                                if (!tagText.isEmpty()) {
                                    tags.add(tagText);
                                }
                            }
                            break;
                    }
                }
            }

            // CSV 다운로드 URL 추출
            String resourceUrl = downloadParamExtractor.extractCsvDownloadUrl(sourceUrl);
            
            String fileName = title + ".csv";

            Dataset dataset = Dataset.builder()
                    .title(title)
                    .description(description)
                    .sourceUrl(sourceUrl)
                    .resourceUrl(resourceUrl)
                    .organization(organization)
                    .createdDate(parseDate(createdDate.substring(0,createdDate.length() - 1)))
                    .updatedDate(parseDate(updatedDate.substring(0,updatedDate.length() - 1)))
                    .resourceName(fileName)
                    .license(license)
                    .type("CSV")
                    .source("서울 열린데이터 광장")
                    .build();

            return Optional.of(new DatasetWithTag(dataset, tags));

        } catch (Exception e) {
            log.error("데이터셋 파싱 실패 - URL: {}, 오류: {}", sourceUrl, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private LocalDate parseDate(String dateStr) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (Exception e){
            return null;
        }

    }
}
