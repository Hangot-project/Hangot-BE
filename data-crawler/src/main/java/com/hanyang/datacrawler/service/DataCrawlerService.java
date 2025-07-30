package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.exception.SkipPageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCrawlerService {

    private final CrawlerFactory crawlerFactory;

    public void crawlDatasets(String siteName, int pageSize, LocalDate startDate, LocalDate endDate) {
        String dateInfo = startDate + " ~ " + endDate + " 기간";
        log.info("{} {} 크롤링 시작", siteName, dateInfo);

        DataCrawler crawler = crawlerFactory.getCrawler(siteName);
        for (int pageNo = 1; ; pageNo++) {
            try{
                List<String> sourceUrlList = crawler.getSourceUrlList(pageNo, pageSize, startDate, endDate);
                if(sourceUrlList.isEmpty()) break;
                crawler.crawlDataset(sourceUrlList);
            } catch (SkipPageException skip) {
                //해당 크롤링 안해도됨
                log.info(skip.getMessage());
            }

        }

        log.info("{} {} 크롤링 완료", siteName, dateInfo);
    }

}