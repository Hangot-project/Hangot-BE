package com.hanyang.dataportal.qna.dto.res;

import com.hanyang.dataportal.qna.domain.Answer;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.user.domain.User;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResAnswerListDto {

    private Integer totalPage;
    private Long totalElement;
    private List<SimpleAnswer> data;
    public ResAnswerListDto(Page<Answer> answers) {
        this.totalPage = answers.getTotalPages();
        this.totalElement = answers.getTotalElements();
        this.data = answers.getContent().stream().map(SimpleAnswer::new).toList();
    }
    @Data
    public static class SimpleAnswer{
        private Long answerId;
        private Long questionId;
        private String title;
        private String content;
        private LocalDate createDate;
        private String adminName;

        public SimpleAnswer(Answer answer) {
            this.answerId = answer.getAnswerId();
            this.title = answer.getTitle();
            this.content = answer.getContent();
            this.createDate = answer.getCreatDate();
            this.questionId = answer.getQuestion().getQuestionId();
            this.adminName = answer.getAdmin().getName();
        }
    }

}
