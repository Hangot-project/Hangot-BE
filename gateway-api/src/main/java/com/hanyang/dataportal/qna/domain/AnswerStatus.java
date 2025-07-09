package com.hanyang.dataportal.qna.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hanyang.dataportal.core.exception.NullException;

import java.util.stream.Stream;

public enum AnswerStatus {
    대기, // 대기
    완료;
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AnswerStatus findByAnswerStatus(String answerStatus) {
        return Stream.of(AnswerStatus.values())
                .filter(o -> o.toString().equals(answerStatus))
                .findFirst()
                .orElseThrow(() -> new NullException("해당 답변 상태는 존재하지 않습니다"));
    }
}
