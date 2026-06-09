package com.docmind.docmind_api.auth.controller;

import com.docmind.docmind_api.auth.dto.LoginRequest;
import com.docmind.docmind_api.auth.dto.LoginResponse;
import com.docmind.docmind_api.auth.dto.RegisterRequest;
import com.docmind.docmind_api.auth.dto.RegisterResponse;
import com.docmind.docmind_api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello Authenticated User";
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public String me(Authentication authentication) {

        return authentication.getName();
    }
}