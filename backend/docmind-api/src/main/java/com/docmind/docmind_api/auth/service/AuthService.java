package com.docmind.docmind_api.auth.service;

import com.docmind.docmind_api.auth.dto.LoginRequest;
import com.docmind.docmind_api.auth.dto.LoginResponse;
import com.docmind.docmind_api.auth.dto.RegisterRequest;
import com.docmind.docmind_api.auth.dto.RegisterResponse;
import com.docmind.docmind_api.auth.entity.User;
import com.docmind.docmind_api.auth.entity.UserRole;
import com.docmind.docmind_api.auth.repository.UserRepository;
import com.docmind.docmind_api.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(
                        request.getEmail())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Invalid credentials"
                        )
                );

        boolean matches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!matches) {
            throw new RuntimeException(
                    "Invalid credentials"
            );
        }

        String token =
                jwtService.generateToken(
                        user.getEmail()
                );

        return new LoginResponse(token);
    }
}