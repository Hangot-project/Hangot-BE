package com.hanyang.dataportal.user.dto.res;

import com.hanyang.dataportal.user.domain.User;
import lombok.Data;

@Data
public class ResUserDto {
    private Long userId;
    private String name;
    private String email;

    public ResUserDto(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
