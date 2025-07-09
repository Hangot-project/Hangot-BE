package com.hanyang.dataportal.qna.repository;

import com.hanyang.dataportal.qna.domain.AnswerStatus;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.qna.domain.QuestionCategory;
import com.hanyang.dataportal.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    Page<Question> findByCategoryAndAnswerStatus(QuestionCategory questionCategory, AnswerStatus answerStatus, Pageable pageable);
    Page<Question> findByUser(User user, Pageable pageable);
    @Query("select q from Question q inner join fetch q.user u where q.questionId = :questionId")
    Optional<Question> findById(Long questionId);
    Page<Question> findByAnswerStatus(AnswerStatus answerStatus,Pageable pageable);
    Page<Question> findByCategory(QuestionCategory questionCategory,Pageable pageable);
}
