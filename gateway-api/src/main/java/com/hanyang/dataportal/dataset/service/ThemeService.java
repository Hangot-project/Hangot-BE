package com.hanyang.dataportal.dataset.service;

import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    public List<Theme> getAllTheme(){
        List<Theme> themeList = new ArrayList<>();
        Collections.addAll(themeList, Theme.values());
        return themeList;
    }
}
