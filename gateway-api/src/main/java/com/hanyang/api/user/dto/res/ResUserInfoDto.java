package com.hanyang.api.user.dto.res;

import com.hanyang.api.user.domain.User;
import lombok.Data;

@Data
public class ResUserInfoDto {

    private String name;
    private String email;
    private int scrapCount;
    private int downloadCount;
    private boolean isSocialLogin;

    public ResUserInfoDto(User user, int scrapCount, int downloadCount,boolean isSocialLogin) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.scrapCount = scrapCount;
        this.downloadCount = downloadCount;
        this.isSocialLogin = isSocialLogin;
    }
}
