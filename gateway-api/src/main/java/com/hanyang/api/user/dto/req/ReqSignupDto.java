package com.hanyang.api.user.dto.req;

import com.hanyang.api.user.dto.OauthUserDto;
import lombok.Data;

@Data
public class ReqSignupDto {
    private String email;
    private String password;
    private String name;

    public ReqSignupDto convertFrom(OauthUserDto oauthUserDto) {
        this.email = oauthUserDto.getEmail();
        this.password = oauthUserDto.getPassword();
        this.name = oauthUserDto.getName();
        return this;
    }
}
