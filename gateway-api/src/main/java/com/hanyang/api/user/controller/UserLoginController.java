package com.hanyang.api.user.controller;

import com.hanyang.api.core.response.ApiResponse;
import com.hanyang.api.user.dto.ResLoginDto;
import com.hanyang.api.user.service.SocialLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/login")
public class UserLoginController {
    private final SocialLoginService socialLoginService;

    @Operation(summary = "소셜 로그인")
    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<ResLoginDto>> socialLogin(
            @PathVariable String provider,
            @RequestParam String code
    ) {
        ResLoginDto responseDto = socialLoginService.socialLogin(provider, code);
        return ResponseEntity.ok().body(ApiResponse.ok(responseDto));
    }

}
