package com.hanyang.dataportal.qna.dto.res;

import com.hanyang.dataportal.qna.domain.Answer;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResAnswerDto {
    private Long answerId;
    private String title;
    private String content;
    private LocalDate createDate;

    public ResAnswerDto(Answer answer) {
        this.answerId = answer.getAnswerId();
        this.title = answer.getTitle();
        this.content = answer.getContent();
        this.createDate = answer.getCreatDate();
    }
}
