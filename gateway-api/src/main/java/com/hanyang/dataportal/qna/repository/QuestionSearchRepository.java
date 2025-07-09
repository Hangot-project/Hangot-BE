package com.hanyang.dataportal.qna.repository;

import com.hanyang.dataportal.qna.domain.Question;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hanyang.dataportal.qna.domain.AnswerStatus.findByAnswerStatus;
import static com.hanyang.dataportal.qna.domain.QQuestion.question;
import static com.hanyang.dataportal.qna.domain.QuestionCategory.findByQuestionCategory;

@Repository
@RequiredArgsConstructor
public class QuestionSearchRepository {
    private final JPAQueryFactory queryFactory;
    private final int PAGE_SIZE = 10;
    public Page<Question> searchQuestionList(String category,String answerStatus,int page){
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        JPAQuery<Question> query = queryFactory.selectFrom(question)
                .where(categoryEq(category), answerStatusEq(answerStatus),question.isOpen.eq(true));

        List<Question> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

        Long count = queryFactory.select(question.count())
                .from(question)
                .where(categoryEq(category),
                        answerStatusEq(answerStatus))
                .fetchOne();

        return new PageImpl<>(content, pageable, count);

    }

    private BooleanExpression categoryEq(String category) {
        return category != null ? question.category.eq(findByQuestionCategory(category)) : null;
    }

    private BooleanExpression answerStatusEq(String answerStatus) {
        return answerStatus !=null  ? question.answerStatus.eq(findByAnswerStatus(answerStatus)) : null;
    }

}
