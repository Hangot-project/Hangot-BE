package com.hanyang.dataportal.qna.service;

import com.hanyang.dataportal.core.exception.ResourceExistException;
import com.hanyang.dataportal.core.exception.ResourceNotFoundException;
import com.hanyang.dataportal.qna.domain.Answer;
import com.hanyang.dataportal.qna.domain.AnswerStatus;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.qna.dto.req.ReqAnswerDto;
import com.hanyang.dataportal.qna.repository.AnswerRepository;
import com.hanyang.dataportal.qna.repository.QuestionRepository;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserService userService;

    public Answer save(ReqAnswerDto reqAnswerDto, Long questionId, String username) {
        User user = userService.findByEmail(username);
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("해당 질문글은 존재하지 않습니다"));
        question.setAnswerStatus(AnswerStatus.완료);
        if (answerRepository.findByQuestion(question).isPresent()) {
            throw new ResourceExistException("해당 질문에 대한 답변이 이미 존재합니다");
        }
        Answer answer = reqAnswerDto.toEntity();
        answer.setAdmin(user);
        answer.setQuestion(question);
        return answerRepository.save(answer);
    }

    public Answer update(ReqAnswerDto reqAnswerDto, Long answerId) {
        Answer answer = findByQuestionId(answerId);
        answer.update(reqAnswerDto);
        return answer;
    }

    public void delete(Long answerId) {
        answerRepository.delete(findByQuestionId(answerId));
    }

    public Answer findByQuestionId(Long questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("해당 질문글은 존재하지 않습니다"));
        return answerRepository.findByQuestion(question).orElseThrow(() -> new ResourceNotFoundException("해당 질문에 대한 답변은 존재하지 않습니다"));
    }
}
