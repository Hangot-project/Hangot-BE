package com.hanyang.dataportal.dataset.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hanyang.dataportal.core.exception.NullException;

import java.util.stream.Stream;

public enum Theme {
    입학, 학생, 학사, 국제, 복지, 재정, 취창업, 학술, 장학;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Theme findByTheme(String theme) {
        return Stream.of(Theme.values())
                .filter(o -> o.toString().equals(theme))
                .findFirst()
                .orElseThrow(() -> new NullException("해당 주제는 존재하지 않습니다"));
    }
}
