package com.hanyang.datacrawler.service.crawler.datago;

public record FileDownloadParams(
        String publicDataPk,
        String publicDataDetailPk,
        String atchFileId,
        String fileDetailSn
) {}
