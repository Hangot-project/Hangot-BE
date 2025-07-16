package com.hanyang.api.user.controller;

import com.hanyang.api.core.jwt.component.AuthorizationExtractor;
import com.hanyang.api.core.jwt.component.JwtTokenProvider;
import com.hanyang.api.core.jwt.component.JwtTokenResolver;
import com.hanyang.api.core.jwt.dto.TokenDto;
import com.hanyang.api.core.response.ApiResponse;
import com.hanyang.api.user.dto.ResLoginDto;
import com.hanyang.api.user.service.UserLoginService;
import com.hanyang.api.user.service.UserLogoutService;
import com.hanyang.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Tag(name = "유저 권한 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserAuthController {
    private final UserService userService;
    private final UserLoginService userLoginService;
    private final UserLogoutService userLogoutService;
    private final JwtTokenResolver jwtTokenResolver;

    @Operation(summary = "유저 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            @CookieValue(JwtTokenProvider.REFRESH_COOKIE_KEY) String refreshToken
    ) {
        userLogoutService.logout(userDetails);
        final ResponseCookie responseCookie = userLogoutService.generateRefreshCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ApiResponse.ok(null));
    }

    @Operation(summary = "유저의 액세스 토큰 재발급")
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<ResLoginDto>> reissueAccessToken(
            @CookieValue(JwtTokenProvider.REFRESH_COOKIE_KEY) final String refreshToken
    ) {
        final TokenDto tokenDto = userLoginService.reissueToken(refreshToken);
        //? request cookie의 만료시간은 읽어올 수 없음(-> RTR 적용시 자동로그인 여부에 따라 refresh token 만료시간을 다르게 해야하는데 할 수 없음)
        final ResponseCookie responseCookie = userLoginService.generateRefreshCookie(tokenDto);
        final String role = jwtTokenResolver.getAuthentication(tokenDto.getAccessToken()).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ApiResponse.ok(new ResLoginDto(AuthorizationExtractor.AUTH_TYPE, tokenDto.getAccessToken(),role)));
    }

//    @Operation(summary = "유저 내정보 확인")
//    @GetMapping
//    public ResponseEntity<ApiResponse<ResUserInfoDto>> myInfo(@AuthenticationPrincipal UserDetails userDetail){
//        return ResponseEntity.ok(ApiResponse.ok(userService.findLoginUserInfo(userDetail.getUsername())));
//    }
//
//    @Operation(summary = "유저 탈퇴")
//    @DeleteMapping
//    public ResponseEntity<ApiResponse<?>> delete(@AuthenticationPrincipal UserDetails userDetail) {
//        userService.delete(userDetail.getUsername());
//        return ResponseEntity.ok(ApiResponse.ok(null));
//    }
}
