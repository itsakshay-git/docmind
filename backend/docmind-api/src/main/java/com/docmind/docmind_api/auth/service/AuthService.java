package com.docmind.docmind_api.auth.service;

import com.docmind.docmind_api.auth.dto.RegisterRequest;
import com.docmind.docmind_api.auth.dto.RegisterResponse;
import com.docmind.docmind_api.auth.entity.User;
import com.docmind.docmind_api.auth.entity.UserRole;
import com.docmind.docmind_api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(UserRole.USER);

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId().toString(),
                saved.getEmail()
        );
    }
}