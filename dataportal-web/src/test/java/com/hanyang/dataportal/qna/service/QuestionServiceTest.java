//package com.hanyang.dataportal.qna.service;
//
//import com.hanyang.dataportal.qna.domain.Question;
//import com.hanyang.dataportal.qna.dto.req.ReqQuestionDto;
//import com.hanyang.dataportal.qna.repository.QuestionRepository;
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
//import org.springframework.web.multipart.MultipartFile;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@ActiveProfiles("test")
//@Sql("/h2-truncate.sql")
//@Transactional
//public class QuestionServiceTest {
//
//    @Autowired
//    private QuestionService questionService;
//    @Autowired
//    private QuestionRepository questionRepository;
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    @DisplayName("질문글을 등록한다(첨부자료가 없을 경우)")
//    void save(){
//        //Given
//        User user = User.builder().email("test@email.com").build();
//        ReqQuestionDto reqQuestionDto = ReqQuestionDto.builder().title("테스트").build();
//        MultipartFile file = null;
//        userRepository.save(user);
//
//        //When
//        Question question = questionService.save(reqQuestionDto, user.getEmail(), file);
//
//        //Then
//        Assertions.assertThat(questionRepository.findById(question.getQuestionId())).isPresent();
//        Assertions.assertThat(question.getUser().getEmail()).isEqualTo(user.getEmail());
//    }
//
//    @Test
//    @DisplayName("질문글을 수정한다")
//    void update() {
//        //Given
//        User user = User.builder().email("test@email.com").build();
//        ReqQuestionDto reqQuestionDto = ReqQuestionDto.builder().title("테스트").build();
//        MultipartFile file = null;
//        userRepository.save(user);
//        Question question = questionService.save(reqQuestionDto, user.getEmail(), file);
//        ReqQuestionDto reqQuestionDtoUpdate = ReqQuestionDto.builder().title("테스트수정").build();
//
//        //When
//        Question getQuestion_modify = questionService.update(reqQuestionDtoUpdate,question.getQuestionId());
//
//        //That
//        Assertions.assertThat(getQuestion_modify.getTitle()).isEqualTo("테스트수정");
//    }
//
//
//    @Test
//    @DisplayName("질문글을 조회한다")
//    void find() {
//        //Given
//        User user = User.builder().email("test@email.com").build();
//        ReqQuestionDto reqQuestionDto = ReqQuestionDto.builder().title("테스트").build();
//        MultipartFile file = null;
//        userRepository.save(user);
//        Question question = questionService.save(reqQuestionDto, user.getEmail(), file);
//
//        //When
//        Question getDetailQuestion = questionService.getDetail(question.getQuestionId());
//
//        //Then
//        Assertions.assertThat(getDetailQuestion.getQuestionId()).isEqualTo(getDetailQuestion.getQuestionId());
//        Assertions.assertThat(getDetailQuestion.getTitle()).isEqualTo("테스트");
//    }
//
//
//    @Test
//    @DisplayName("질문글을 삭제한다")
//    void delete(){
//        //Given
//        User user = User.builder().email("test@email.com").build();
//        ReqQuestionDto reqQuestionDto = ReqQuestionDto.builder().title("테스트").build();
//        MultipartFile file = null;
//        userRepository.save(user);
//        Question question = questionService.save(reqQuestionDto, user.getEmail(), file);
//
//        //When
//        questionService.delete(question.getQuestionId());
//
//        //Then
//        Assertions.assertThat(questionRepository.findById(question.getQuestionId()).isEmpty());
//    }
//}
