package com.hanyang.dataportal.dataset.dto.res;

import com.hanyang.dataportal.qna.domain.QuestionCategory;
import lombok.Data;

import java.util.List;

@Data
public class ResQuestionCategoryListDto {
    List<QuestionCategory> questionCategoryList;

    public ResQuestionCategoryListDto(List<QuestionCategory> questionCategoryList) {
        this.questionCategoryList = questionCategoryList;
    }
}
