package com.hanyang.dataportal.qna.dto.req;


import com.hanyang.dataportal.qna.domain.AnswerStatus;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.qna.domain.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqQuestionDto {
    private QuestionCategory category;
    private String title;
    private String content;
    private boolean isOpen;

    public Question toEntity() {
        return Question.builder()
                .title(title)
                .content(content)
                .createDate(LocalDate.now())
                .view(0)
                .answerStatus(AnswerStatus.대기)
                .category(category)
                .isOpen(isOpen)
                .build();
    }
}