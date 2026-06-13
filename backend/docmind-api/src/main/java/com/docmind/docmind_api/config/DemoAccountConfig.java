package com.docmind.docmind_api.config;

import com.docmind.docmind_api.auth.entity.User;
import com.docmind.docmind_api.auth.entity.UserRole;
import com.docmind.docmind_api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DemoAccountConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner demoAccountInitializer(
            @Value("${docmind.demo.enabled:true}") boolean enabled,
            @Value("${docmind.demo.email}") String email,
            @Value("${docmind.demo.password}") String password,
            @Value("${docmind.demo.full-name}") String fullName
    ) {

        return args -> {
            if (!enabled || userRepository.existsByEmail(email)) {
                return;
            }

            User user =
                    new User();

            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(UserRole.USER);
            user.setPasswordHash(
                    passwordEncoder.encode(password)
            );

            userRepository.save(user);
        };
    }
}
