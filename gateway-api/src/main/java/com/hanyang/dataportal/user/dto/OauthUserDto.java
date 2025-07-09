package com.hanyang.dataportal.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OauthUserDto {
    private String email;
    private String password;
    private String name;
}
