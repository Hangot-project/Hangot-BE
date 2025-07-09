package com.hanyang.dataportal.qna.service;

import com.hanyang.dataportal.core.exception.ResourceNotFoundException;
import com.hanyang.dataportal.qna.domain.Question;
import com.hanyang.dataportal.qna.domain.QuestionCategory;
import com.hanyang.dataportal.qna.dto.req.ReqQuestionDto;
import com.hanyang.dataportal.qna.repository.QuestionRepository;
import com.hanyang.dataportal.qna.repository.QuestionSearchRepository;
import com.hanyang.dataportal.resource.infrastructure.S3StorageManager;
import com.hanyang.dataportal.resource.infrastructure.dto.FileInfoDto;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final QuestionSearchRepository questionSearchRepository;
    private final S3StorageManager s3StorageManager;
    private final static int PAGE_SIZE = 10;
    private final static String folderName = "Question";

    public Question save(ReqQuestionDto reqQuestionDto, String email, MultipartFile file) {
        User user = userService.findByEmail(email);
        Question question = reqQuestionDto.toEntity();
        question.setUser(user);

        if (file != null) {
            FileInfoDto fileInfoDto = s3StorageManager.uploadFile(folderName,question.getQuestionId(), file);
            question.setImageUrl(fileInfoDto.getUrl());
        }

        return questionRepository.save(question);
    }

    public Question update(ReqQuestionDto reqQuestionDto, long questionId) {
        Question question = findById(questionId);
        question.update(reqQuestionDto);
        return question;
    }

    public Question getDetail(Long questionId) {
        Question question = findById(questionId);
        question.updateView();
        return question;
    }

    public void delete(Long questionId) {
        questionRepository.delete(findById(questionId));
    }

    @Transactional(readOnly = true)
    public Page<Question> getQuestionList(int pageNum, String category, String answerStatus) {
        return questionSearchRepository.searchQuestionList(category, answerStatus, pageNum);
    }

    @Transactional(readOnly = true)
    public Page<Question> getMyQuestionList(String userName, int pageNum) {
        User user = userService.findByEmail(userName);
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE);
        return questionRepository.findByUser(user, pageable);
    }

    public List<QuestionCategory> getQuestionCategoryList() {
        List<QuestionCategory> questionCategoryList = new ArrayList<>();
        Collections.addAll(questionCategoryList, QuestionCategory.values());
        return questionCategoryList;
    }

    private Question findById(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("해당 질문글은 존재하지 않습니다"));
    }
}

