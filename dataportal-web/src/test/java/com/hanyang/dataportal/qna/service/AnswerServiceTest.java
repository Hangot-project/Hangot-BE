//package com.hanyang.dataportal.qna.service;
//
//import com.hanyang.dataportal.qna.domain.Answer;
//import com.hanyang.dataportal.qna.domain.Question;
//import com.hanyang.dataportal.qna.dto.req.ReqAnswerDto;
//import com.hanyang.dataportal.qna.repository.AnswerRepository;
//import com.hanyang.dataportal.qna.repository.QuestionRepository;
//import com.hanyang.dataportal.user.domain.Role;
//import com.hanyang.dataportal.user.domain.User;
//import com.hanyang.dataportal.user.repository.UserRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@ActiveProfiles("test")
//@Sql("/h2-truncate.sql")
//@Transactional
//public class AnswerServiceTest {
//
//    @Autowired
//    private AnswerRepository answerRepository;
//    @Autowired
//    private AnswerService answerService;
//    @Autowired
//    private QuestionRepository questionRepository;
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    @DisplayName("답변글을 등록한다")
//    void save(){
//        //given
//        User admin = User.builder().email("admin@email.com").role(Role.ROLE_ADMIN).build();
//        userRepository.save(admin);
//        questionRepository.save(Question.builder().questionId(1L).build());
//        ReqAnswerDto reqAnswerDto = ReqAnswerDto.builder().title("답변").build();
//
//        //when
//        Answer answer = answerService.save(reqAnswerDto, 1L,admin.getEmail());
//
//        //then
//        Assertions.assertThat(answerRepository.findById(answer.getAnswerId())).isPresent();
//        Assertions.assertThat(answerRepository.findById(answer.getAnswerId())).isPresent();
//
//    }
//
//    @Test
//    @DisplayName("답변을 수정한다")
//    void update() {
//        //given
//        User admin = User.builder().email("admin@email.com").role(Role.ROLE_ADMIN).build();
//        userRepository.save(admin);
//        Question question = questionRepository.save(Question.builder().build());
//        ReqAnswerDto reqAnswerDto = ReqAnswerDto.builder().title("답변").build();
//
//        //when
//        Answer answer = answerService.save(reqAnswerDto,question.getQuestionId(), admin.getEmail());
//
//        //then
//        Assertions.assertThat(answerRepository.findById(answer.getAnswerId())).isPresent();
//    }
//
//    @Test
//    @DisplayName("답변을 조회 한다")
//    void find(){
//        //given
//        User admin = User.builder().email("admin@email.com").role(Role.ROLE_ADMIN).build();
//        userRepository.save(admin);
//        Question question = questionRepository.save(Question.builder().build());
//        Answer savedAnswer = answerRepository.save(Answer.builder().question(question).build());
//
//        //when
//        Answer answer = answerService.findByQuestionId(question.getQuestionId());
//
//        //then
//        Assertions.assertThat(answer.getAnswerId()).isEqualTo(savedAnswer.getAnswerId());
//    }
//}
