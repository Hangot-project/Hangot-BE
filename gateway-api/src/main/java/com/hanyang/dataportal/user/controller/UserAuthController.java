package com.hanyang.dataportal.user.controller;

import com.hanyang.dataportal.core.jwt.component.AuthorizationExtractor;
import com.hanyang.dataportal.core.jwt.component.JwtTokenProvider;
import com.hanyang.dataportal.core.jwt.component.JwtTokenResolver;
import com.hanyang.dataportal.core.jwt.dto.TokenDto;
import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.dto.req.*;
import com.hanyang.dataportal.user.dto.res.ResCodeDto;
import com.hanyang.dataportal.user.dto.res.ResLoginDto;
import com.hanyang.dataportal.user.dto.res.ResUserDto;
import com.hanyang.dataportal.user.dto.res.ResUserInfoDto;
import com.hanyang.dataportal.user.infrastructure.EmailManager;
import com.hanyang.dataportal.user.service.*;
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
    private final EmailManager emailManager;
    private final JwtTokenResolver jwtTokenResolver;

    @Operation(summary = "이메일로 인증 번호 받기")
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<?>> email(@RequestBody ReqEmailDto reqSignupDto){
        return ResponseEntity.ok(ApiResponse.ok(new ResCodeDto(emailManager.joinEmail(reqSignupDto.getEmail()))));
    }

    @Operation(summary = "인증 번호 인증")
    @PostMapping("/code")
    public ResponseEntity<ApiResponse<?>> codeCheck(@RequestBody ReqCodeDto reqCodeDto){
        emailManager.checkCode(reqCodeDto);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "유저 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<ResUserDto>> signup(@RequestBody ReqSignupDto reqSignupDto){
        return ResponseEntity.ok(ApiResponse.ok(new ResUserDto(userService.signUp(reqSignupDto))));
    }

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

    @Operation(summary = "유저 기존 비밀번호 확인")
    @GetMapping("/password/check")
    public ResponseEntity<ApiResponse<?>> passwordCheck(@AuthenticationPrincipal UserDetails userDetail, @RequestBody ReqPasswordDto reqPasswordDto){
        userLoginService.passwordCheck(userDetail,reqPasswordDto);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "유저 비밀번호 변경")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<?>> passwordChange(@AuthenticationPrincipal UserDetails userDetail,@RequestBody ReqPasswordDto reqPasswordDto){
        userLoginService.changePassword(userDetail,reqPasswordDto.getPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "유저 비밀번호 분실시 임시 비밀번호 발급")
    @GetMapping("/password")
    public ResponseEntity<ApiResponse<?>> passwordChange(@AuthenticationPrincipal UserDetails userDetail){
        userLoginService.findPassword(userDetail);
        return ResponseEntity.ok(ApiResponse.ok(null));
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

    @Operation(summary = "유저 내정보 확인")
    @GetMapping
    public ResponseEntity<ApiResponse<ResUserInfoDto>> myInfo(@AuthenticationPrincipal UserDetails userDetail){
        return ResponseEntity.ok(ApiResponse.ok(userService.findLoginUserInfo(userDetail.getUsername())));
    }

    @Operation(summary = "유저 이름 변경")
    @PutMapping("/name")
    public ResponseEntity<ApiResponse<ResUserDto>> nameUpdate(@AuthenticationPrincipal UserDetails userDetail,@RequestBody String userName) {
        User user = userService.updateName(userDetail.getUsername(), userName);
        return ResponseEntity.ok(ApiResponse.ok(new ResUserDto(user)));
    }

    @Operation(summary = "유저 탈퇴")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> delete(@AuthenticationPrincipal UserDetails userDetail) {
        userService.delete(userDetail.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
