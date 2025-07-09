package com.hanyang.dataportal.notice.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hanyang.dataportal.core.exception.NullException;

import java.util.stream.Stream;

public enum NoticeLabel {
    일반, 업데이트, 중요;
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NoticeLabel findByNoticeLabel(String label) {
        return Stream.of(NoticeLabel.values())
                .filter(o -> o.toString().equals(label))
                .findFirst()
                .orElseThrow(() -> new NullException("해당 라벨은 존재하지 않습니다"));
    }
}
