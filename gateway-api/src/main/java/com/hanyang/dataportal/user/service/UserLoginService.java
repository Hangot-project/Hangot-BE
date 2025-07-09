package com.hanyang.dataportal.user.service;

import com.hanyang.dataportal.core.exception.TokenExpiredException;
import com.hanyang.dataportal.core.exception.UnAuthenticationException;
import com.hanyang.dataportal.core.jwt.component.JwtTokenProvider;
import com.hanyang.dataportal.core.jwt.component.JwtTokenResolver;
import com.hanyang.dataportal.core.jwt.component.JwtTokenValidator;
import com.hanyang.dataportal.core.jwt.dto.TokenDto;
import com.hanyang.dataportal.core.response.ResponseMessage;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.dto.req.ReqLoginDto;
import com.hanyang.dataportal.user.dto.req.ReqPasswordDto;
import com.hanyang.dataportal.user.infrastructure.EmailManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLoginService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenResolver jwtTokenResolver;
    private final UserService userService;
    private final EmailManager emailManager;
    private final PasswordEncoder passwordEncoder;

    public TokenDto login(ReqLoginDto reqLoginDto) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(reqLoginDto.getEmail(), reqLoginDto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        final Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // JWT 토큰 생성
        return jwtTokenProvider.generateLoginToken(authentication, reqLoginDto.getAutoLogin());
    }

    /**
     * 액세스 토큰(+ 리프레시 토큰)을 재발급하는 메서드
     * @param refreshToken
     * @return
     * @throws TokenExpiredException
     */
    public TokenDto reissueToken(final String refreshToken) throws TokenExpiredException {
        final Authentication authentication = jwtTokenResolver.getAuthentication(refreshToken);
        final boolean autoLogin = jwtTokenResolver.getAutoLogin(refreshToken);
        if (jwtTokenValidator.validateToken(refreshToken)) {
            return jwtTokenProvider.generateLoginToken(authentication, autoLogin);
        }
        throw new TokenExpiredException(ResponseMessage.REFRESH_EXPIRED);
    }

    /**
     * refresh token 쿠키를 리턴하는 메서드
     * @param tokenDto
     * @return
     */
    public ResponseCookie generateRefreshCookie(final TokenDto tokenDto) {
        return jwtTokenProvider.generateRefreshCookie(
                tokenDto.getRefreshToken(),
                jwtTokenResolver.getAutoLogin(tokenDto.getAccessToken())
        );
    }

    public void passwordCheck(UserDetails userDetails, ReqPasswordDto reqPasswordDto){
        User user = userService.findByEmail(userDetails.getUsername());
        //일치하면
        if(passwordEncoder.matches(reqPasswordDto.getPassword(),user.getPassword())) {
            return;
        }
        throw new UnAuthenticationException(ResponseMessage.WRONG_PASSWORD);
    }
    public void changePassword(UserDetails userDetails,String newPassword){
        User user = userService.findByEmail(userDetails.getUsername());
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    public void findPassword(UserDetails userDetails){
        User user = userService.findByEmail(userDetails.getUsername());
        String temporaryPassword = emailManager.temporaryPasswordEmail(user.getPassword());
        changePassword(userDetails,temporaryPassword);
    }
}
