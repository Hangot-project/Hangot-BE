package com.hanyang.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResLoginDto {
    private String grantType;
    private String accessToken;
    private String role;
}
