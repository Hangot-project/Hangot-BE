package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.ResourceExistException;
import com.hanyang.api.core.exception.ResourceNotFoundException;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.dto.OauthUserDto;
import com.hanyang.api.user.dto.res.ResUserInfoDto;
import com.hanyang.api.user.repository.ScrapRepository;
import com.hanyang.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hanyang.api.core.response.ResponseMessage.DUPLICATE_EMAIL;
import static com.hanyang.api.core.response.ResponseMessage.NOT_EXIST_USER;
import static com.hanyang.api.user.domain.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException(NOT_EXIST_USER));
    }

    public User signUpOauth(OauthUserDto oauthUserDto){
        if(!isExistByEmail(oauthUserDto.getEmail()) && !isExistByName(oauthUserDto.getName())) {
            User user = User.builder()
                    .email(oauthUserDto.getEmail())
                    .name(oauthUserDto.getName())
                    .role(ROLE_USER)
                    .build();
            return userRepository.save(user);
        }

        throw new ResourceExistException(DUPLICATE_EMAIL);
    }

    public ResUserInfoDto findLoginUserInfo(String email){
        User user = findByEmail(email);
        int scrapCount =  scrapRepository.countByUser(user);
        if(user.getEmail().matches("\\d+")){
            return new ResUserInfoDto(user,scrapCount,true);
        }
        else{
            return new ResUserInfoDto(user,scrapCount,false);
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
