package com.hanyang.dataportal.qna.controller;

import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.qna.dto.req.ReqQuestionDto;
import com.hanyang.dataportal.qna.dto.res.ResQuestionDto;
import com.hanyang.dataportal.qna.dto.res.ResQuestionListDto;
import com.hanyang.dataportal.qna.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "질문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuestionController {
    private final QuestionService questionService;
    @Operation(summary = "질문글 작성")
    @PostMapping(value = "/question", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResQuestionDto>> createQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                                                      @RequestPart ReqQuestionDto reqQuestionDto, @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        Question question = questionService.save(reqQuestionDto, userDetails.getUsername(), multipartFile);
        return ResponseEntity.ok(ApiResponse.ok(new ResQuestionDto(question)));
    }
    @Operation(summary = "질문글 수정")
    @PutMapping( "/question/{questionId}")
    public ResponseEntity<ApiResponse<ResQuestionDto>> updateQuestion(@RequestBody ReqQuestionDto reqQuestionDto, @PathVariable long questionId) {
        Question question = questionService.update(reqQuestionDto, questionId);
        return ResponseEntity.ok(ApiResponse.ok(new ResQuestionDto(question)));
    }
    @Operation(summary = "질문글 삭제")
    @DeleteMapping(value = "/question/{questionId}")
    public ResponseEntity<ApiResponse<?>> deleteQuestion(@PathVariable long questionId) {
        questionService.delete(questionId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @Operation(summary = "카테고리별 질문글 조회")
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<ResQuestionListDto>> getQuestionList(@RequestParam Integer page,
                                                                           @RequestParam(required = false) String category,
                                                                           @RequestParam(required = false) String answerStatus) {
        Page<Question> questions = questionService.getQuestionList(page,category,answerStatus);
        return ResponseEntity.ok(ApiResponse.ok(new ResQuestionListDto(questions)));
   }
    @Operation(summary = "나의 질문글 내역리스트")
    @GetMapping("/my/questions")
    public ResponseEntity<ApiResponse<ResQuestionListDto>> getMyQuestionList(@AuthenticationPrincipal UserDetails userDetails, @RequestParam int page) {
        String userName = userDetails.getUsername();
        Page<Question> questions = questionService.getMyQuestionList(userName, page);
        return ResponseEntity.ok(ApiResponse.ok(new ResQuestionListDto(questions)));
   }
    @Operation(summary = "질문글 상세 조회")
    @GetMapping("/question/{questionId}")
    public ResponseEntity<ApiResponse<ResQuestionDto>> getQuestion(@PathVariable Long questionId) {
        Question question = questionService.getDetail(questionId);
        return ResponseEntity.ok(ApiResponse.ok(new ResQuestionDto(question)));
    }
}