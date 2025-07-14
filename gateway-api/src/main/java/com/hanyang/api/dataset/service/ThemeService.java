package com.hanyang.api.dataset.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    public List<String> getAllTheme(){
        List<String> themeList = new ArrayList<>();
        themeList.add("교통");
        themeList.add("안전");
        themeList.add("보건");
        themeList.add("환경");
        themeList.add("복지");
        themeList.add("문화");
        themeList.add("농축산");
        themeList.add("산업");
        themeList.add("기타");
        return themeList;
    }
}
