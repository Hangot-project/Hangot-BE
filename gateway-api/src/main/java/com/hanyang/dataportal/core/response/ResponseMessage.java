package com.hanyang.dataportal.core.response;

public class ResponseMessage {
    public static String SUCCESS ="ok";
    public static String NOT_EXIST_DATASET ="해당 데이터셋은 존재하지 않습니다";
    public static String NOT_EXIST_USER ="해당 유저는 존재하지 않습니다";
    public static String NOT_EXIST_SCRAP = "해당 스크랩은 존재하지 않습니다.";
    public static String DUPLICATE_EMAIL ="중복되는 이메일이 존재합니다";
    public static String DUPLICATE_SCRAP = "이미 존재하는 스크랩 내역입니다.";
    public static String INVALID_JWT ="유효하지 않은 토큰입니다";
    public static String WRONG_PASSWORD ="잘못된 비밀번호 입니다";
    public static String ACCESS_DENIED ="접근 권한이 없습니다";
    public static String UN_AUTHORIZED ="인증되지 않은 사용자 입니다";
    public static String INVALID_ACCESS = "액세스 토큰이 유효하지 않습니다";
    public static String ACCESS_EXPIRED = "ACCESS_EXPIRED"; // 프론트와 공유
    public static String REFRESH_EXPIRED = "REFRESH_EXPIRED"; // 프론트와 공유
    public static String FILE_ERROR ="잘못된 형태의 파일입니다";
    public static String AUTHENTICATION_FAILED = "인증에 실패하였습니다";
    public static String NOT_EXIST_RESOURCE = "해당 리소스는 존재하지 않습니다";
    public static String ILLEGAL_PROVIDER = "지원하지 않는 소셜 로그인 입니다";
}

