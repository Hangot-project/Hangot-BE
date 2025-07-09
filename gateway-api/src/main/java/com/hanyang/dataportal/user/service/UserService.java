package com.hanyang.dataportal.user.service;

import com.hanyang.dataportal.core.exception.ResourceExistException;
import com.hanyang.dataportal.core.exception.ResourceNotFoundException;
import com.hanyang.dataportal.resource.repository.DownloadRepository;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.dto.req.ReqSignupDto;
import com.hanyang.dataportal.user.dto.res.ResUserInfoDto;
import com.hanyang.dataportal.user.repository.ScrapRepository;
import com.hanyang.dataportal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hanyang.dataportal.core.response.ResponseMessage.DUPLICATE_EMAIL;
import static com.hanyang.dataportal.core.response.ResponseMessage.NOT_EXIST_USER;
import static com.hanyang.dataportal.user.domain.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DownloadRepository downloadRepository;
    private final ScrapRepository scrapRepository;
    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException(NOT_EXIST_USER));
    }

    public User signUp(ReqSignupDto reqSignupDto){

        if(!isExistByEmail(reqSignupDto.getEmail()) && !isExistByName(reqSignupDto.getName())) {
            User user = User.builder()
                    .email(reqSignupDto.getEmail())
                    .password(passwordEncoder.encode(reqSignupDto.getPassword()))
                    .name(reqSignupDto.getName())
                    .role(ROLE_USER)
                    .build();
            return userRepository.save(user);
        }

        throw new ResourceExistException(DUPLICATE_EMAIL);
    }

    public ResUserInfoDto findLoginUserInfo(String email){
        User user = findByEmail(email);
        int scrapCount =  scrapRepository.countByUser(user);
        int downloadCount = downloadRepository.countByUser(user);
        if(user.getEmail().matches("\\d+")){
            return new ResUserInfoDto(user,scrapCount,downloadCount,true);
        }
        else{
            return new ResUserInfoDto(user,scrapCount,downloadCount,false);
        }
    }

    public User updateName(String email,String name){
        User user = findByEmail(email);
        user.updateName(name);
        return user;
    }

    public void delete(String email){
        User user = findByEmail(email);
        user.withdraw();
    }

    private boolean isExistByName(String name){
        return userRepository.findByName(name).isPresent();
    }

    private boolean isExistByEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }

}
