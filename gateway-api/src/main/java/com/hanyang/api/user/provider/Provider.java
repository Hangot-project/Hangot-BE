package com.hanyang.api.user.provider;

public enum Provider {
    KAKAO("kakao");

    private final String value;

    Provider(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
