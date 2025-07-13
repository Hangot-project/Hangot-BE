package com.hanyang.api.dataset.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hanyang.api.core.exception.NullException;

import java.util.stream.Stream;

public enum Type {
    csv, xls, xlsx, pdf, json, docx, png, jpg, jpeg;
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Type findByType(String type) {
        return Stream.of(Type.values())
                .filter(o -> o.toString().equals(type))
                .findFirst()
                .orElseThrow(() -> new NullException("해당 파일유형은 존재하지 않거나, 지원하지 않습니다."));
    }

    public boolean isExcelType() {
        return this == csv || this == xls || this == xlsx;
    }
}


