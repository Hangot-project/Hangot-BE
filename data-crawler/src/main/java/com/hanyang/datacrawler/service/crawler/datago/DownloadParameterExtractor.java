package com.hanyang.datacrawler.service.crawler.datago;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadParameterExtractor {

    private final RestTemplate restTemplate;

    public Optional<FileDownloadParams> extractDownloadParams(String sourceURL) {
        String html = restTemplate.getForObject(sourceURL, String.class);
        Document doc = Jsoup.parse(html);
        Element downloadBtn = doc.selectFirst("a.button.just-mb[onclick*='fn_fileDataDown']");

        if (downloadBtn == null){
            log.info("다운로드 파일이 없는 데이터셋 : {}",sourceURL);
            return Optional.empty();
        }
        
        String onclick = downloadBtn.attr("onclick");
        String[] args = extractArgsFromOnclick(onclick);

        String publicDataPk = args[0].trim();
        String publicDataDetailPk = args[1].trim();

        return extractFileParams(publicDataPk, publicDataDetailPk);
    }

    public Optional<FileDownloadParams> extractFileParams(String publicDataPK, String publicDataDetailPk) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://www.data.go.kr/tcs/dss/selectFileDataDownload.do")
                .queryParam("publicDataPk", publicDataPK)
                .queryParam("publicDataDetailPk", publicDataDetailPk)
                .queryParam("fileDetailSn", 1)
                .toUriString();


        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Referer", "https://www.data.go.kr/data/" + publicDataDetailPk + "/fileData.do");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            String responseBody = response.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode json = objectMapper.readTree(responseBody);

            if (json.path("status").asBoolean(false)) {
                String atchFileId = json.path("atchFileId").asText();
                String fileDetailSn = json.path("fileDetailSn").asText();
                return Optional.of(new FileDownloadParams(publicDataPK, publicDataDetailPk, atchFileId, fileDetailSn));
            }

        } catch (Exception e) {
            log.error("파일 파라미터 추출 실패 -  에러: {}", e.getMessage());
        }
        return Optional.empty();
    }


    private String[] extractArgsFromOnclick(String onclick) {
        int startIndex = onclick.indexOf("(") + 1;
        int endIndex = onclick.lastIndexOf(")");
        String argsString = onclick.substring(startIndex, endIndex).replace("'", "");
        return argsString.split(",");
    }

}