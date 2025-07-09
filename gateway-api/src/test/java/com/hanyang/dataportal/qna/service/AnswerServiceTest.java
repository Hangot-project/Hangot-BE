package com.hanyang.dataportal.qna.service;

import com.hanyang.dataportal.core.exception.ResourceExistException;
import com.hanyang.dataportal.qna.domain.Answer;
import com.hanyang.dataportal.qna.domain.AnswerStatus;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.qna.dto.req.ReqAnswerDto;
import com.hanyang.dataportal.qna.repository.AnswerRepository;
import com.hanyang.dataportal.qna.repository.QuestionRepository;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Answer Service 테스트")
class AnswerServiceTest {

    @InjectMocks
    private AnswerService answerService;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("답변을 저장할 수 있다.")
    void save_answer_success() {
        // given
        ReqAnswerDto reqAnswerDto = ReqAnswerDto.builder()
                .title("제목")
                .content("본문")
                .build();
        Long questionId = 1L;
        String username = "test@example.com";

        User user = new User();
        Question question = new Question();

        when(userService.findByEmail(username)).thenReturn(user);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestion(question)).thenReturn(Optional.empty());
        when(answerRepository.save(any(Answer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Answer answer = answerService.save(reqAnswerDto, questionId, username);

        // then
        assertNotNull(answer);
        assertEquals(reqAnswerDto.getTitle(), answer.getTitle());
        assertEquals(reqAnswerDto.getContent(), answer.getContent());
    }


    @Test
    @DisplayName("답변을 성공적으로 저장하면 답변의 상태는 완료가 된다.")
    void save_answer_status_complete() {
        // given
        ReqAnswerDto reqAnswerDto = ReqAnswerDto.builder()
                .title("제목")
                .content("본문")
                .build();
        Long questionId = 1L;
        String username = "test@example.com";

        User user = new User();
        Question question = Question.builder().answerStatus(AnswerStatus.대기).build();

        when(userService.findByEmail(username)).thenReturn(user);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestion(question)).thenReturn(Optional.empty());

        // when
        answerService.save(reqAnswerDto, questionId, username);

        // then
        assertEquals(question.getAnswerStatus(), AnswerStatus.완료);
    }

    @Test
    @DisplayName("답변이 이미 존재하면 에러를 반환한다.")
    void answer_error() {
        // given
        ReqAnswerDto reqAnswerDto = ReqAnswerDto.builder()
                .title("제목")
                .content("본문")

                .build();
        Long questionId = 1L;
        String username = "test@example.com";

        User user = new User();
        Question question = new Question();
        Answer answer = new Answer();

        when(userService.findByEmail(username)).thenReturn(user);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestion(question)).thenReturn(Optional.of(answer));

        // when&then
        ResourceExistException exception = assertThrows(
                ResourceExistException.class,
                () -> answerService.save(reqAnswerDto, questionId, username)
        );

        assertEquals("해당 질문에 대한 답변이 이미 존재합니다", exception.getMessage());
    }
}