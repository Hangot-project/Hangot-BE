package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.ResourceNotFoundException;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.provider.Provider;
import com.hanyang.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.hanyang.api.user.domain.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    public User signUp(Provider provider, String providerId) {
        Optional<User> user = userRepository.findByProviderId(providerId);
        return user.orElseGet(() -> User.builder()
                .provider(provider.value())
                .providerId(providerId)
                .role(ROLE_USER)
                .build());
    }
    public User findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId).orElseThrow(() -> new ResourceNotFoundException("해당 유저가 존재하지 않습니다"));
    }
}
