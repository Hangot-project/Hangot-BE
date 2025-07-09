package com.hanyang.dataportal.qna.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hanyang.dataportal.core.exception.NullException;

import java.util.stream.Stream;

public enum QuestionCategory {
    회원정보관리, 문제해결, 서비스이용, 데이터라이선스, 기타;
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static QuestionCategory findByQuestionCategory(String questionCategory) {
        return Stream.of(QuestionCategory.values())
                .filter(o -> o.toString().equals(questionCategory))
                .findFirst()
                .orElseThrow(() -> new NullException("해당 질문 카테고리는 존재하지 않습니다"));
    }
}

