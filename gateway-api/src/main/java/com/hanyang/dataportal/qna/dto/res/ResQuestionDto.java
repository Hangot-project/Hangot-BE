package com.hanyang.dataportal.qna.dto.res;

import com.hanyang.dataportal.qna.domain.AnswerStatus;
import com.hanyang.dataportal.qna.domain.Question;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResQuestionDto {
    private Long questionId;
    private String title;
    private LocalDate date;
    private String content;
    private Integer view;
    private AnswerStatus answerStatus;
    private String userName;
    private String s3Url;
    public ResQuestionDto(Question question) {
        this.questionId = question.getQuestionId();
        this.title = question.getTitle();
        this.date = question.getCreateDate();
        this.view = question.getView();
        this.content = question.getContent();
        this.answerStatus = question.getAnswerStatus();
        this.userName = question.getUser().getName();
        this.s3Url = question.getImageUrl();
    }
}
