package com.hanyang.dataportal.core.component;

import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.hanyang.dataportal.user.domain.Role.ROLE_ADMIN;
import static com.hanyang.dataportal.user.domain.Role.ROLE_USER;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;;

    @Override
    public void run(String... args){

        String rawPassword = "1234";

        if (userRepository.findByEmail("testAdmin").isEmpty()) {
            User testAdmin = User.builder().name("관리자").email("testAdmin").password(passwordEncoder.encode(rawPassword)).role(ROLE_ADMIN).build();
            userRepository.save(testAdmin);
        }

        if (userRepository.findByEmail("testUser").isEmpty()) {
            User testUser = User.builder().name("유저").email("testUser").password(passwordEncoder.encode(rawPassword)).role(ROLE_USER).build();
            userRepository.save(testUser);
        }

    }
}
