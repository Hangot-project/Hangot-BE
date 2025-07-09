package com.hanyang.dataportal.qna.dto.res;


import com.hanyang.dataportal.qna.domain.AnswerStatus;
import com.hanyang.dataportal.qna.domain.Question;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResQuestionListDto {
    private Integer totalPage;
    private Long totalElement;
    private List<SimpleQuestion> data;

    public ResQuestionListDto(Page<Question> question) {
        this.totalPage = question.getTotalPages();
        this.totalElement = question.getTotalElements();
        this.data = question.getContent().stream().map(SimpleQuestion::new).toList();
    }

    @Data
    public static class SimpleQuestion {
        private Long questionId;
        private String title;
        private LocalDate createDate;
        private Integer view;
        private AnswerStatus answerStatus;
        private String userName;

        public SimpleQuestion(Question question) {
            this.questionId = question.getQuestionId();
            this.title = question.getTitle();
            this.createDate = question.getCreateDate();
            this.view = question.getView();
            this.userName = question.getUser().getName();
            this.answerStatus = question.getAnswerStatus();
        }
    }
}
