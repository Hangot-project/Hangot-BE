package com.hanyang.dataportal.user.infrastructure;

import com.hanyang.dataportal.core.exception.UnAuthenticationException;
import com.hanyang.dataportal.core.response.ResponseMessage;
import com.hanyang.dataportal.user.dto.req.ReqCodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailManager {
    private final JavaMailSender mailSender;
    private final RedisManager redisManager;
    @Value("${email.setFrom}")
    private String setFrom;
    private static final String EMAIL_TITLE = "한양대 에리카 DATA 포털 - 회원 가입을 위한 인증 이메일";
    private static final String EMAIL_CONTENT_TEMPLATE = "한양대 에리카 DATA 포털 사이트에 가입해주셔서 감사합니다! " +
            "아래의 인증번호를 입력하여 회원가입을 완료해주세요."+
            "<br><br>" +
            "인증번호 %s";
    private static final String EMAIL_TITLE_TEMPORARY_PASSWORD = "한양대 에리카 DATA 포털 - 임시 비밀번호 발급";
    private static final String EMAIL_CONTENT_TEMPLATE_TEMPORARY_PASSWORD = "한양대 에리카 DATA 포털 임시 비밀번호 입니다. " +
            "아래의 임시번호를 입력하여 로그인 해주세요."+
            "<br><br>" +
            "임시 비밀번호 %s";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String joinEmail(String email) {
        String code = Integer.toString(makeRandomNumber());
        String content = String.format(EMAIL_CONTENT_TEMPLATE, code);
        mailSend(setFrom, email, EMAIL_TITLE,content);
        redisManager.setCode(email,code);
        return code;
    }
    public String temporaryPasswordEmail(String email) {
        String temporaryPassword = generateRandomString();
        String content = String.format(EMAIL_TITLE_TEMPORARY_PASSWORD, temporaryPassword);
        mailSend(setFrom, email, EMAIL_CONTENT_TEMPLATE_TEMPORARY_PASSWORD,content);
        redisManager.setCode(email,temporaryPassword);
        return temporaryPassword;
    }

    public void checkCode(ReqCodeDto reqCodeDto) {
        String code = redisManager.getCode(reqCodeDto.getEmail());
        if(!Objects.equals(code, reqCodeDto.getCode())){
            throw new UnAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED);
        }
    }

    private void mailSend(String setFrom, String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content,true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private int makeRandomNumber() {
        Random r = new Random();
        StringBuilder randomNumber = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            randomNumber.append(r.nextInt(10));
        }
        return Integer.parseInt(randomNumber.toString());
    }

    private String generateRandomString() {
        Random r = new Random();
        StringBuilder stringBuilder = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            stringBuilder.append(CHARACTERS.charAt(r.nextInt(10)));
        }
        return stringBuilder.toString();
    }

}