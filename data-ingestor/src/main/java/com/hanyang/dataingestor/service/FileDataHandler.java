package com.hanyang.dataingestor.service;

import java.util.List;

public interface FileDataHandler {
    List<String> getHeader();
    List<List<String>> getRows();
}