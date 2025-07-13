package com.hanyang.api.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OauthUserDto {
    private String email;
    private String password;
    private String name;
}
