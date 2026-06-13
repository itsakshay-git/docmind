package com.docmind.docmind_api.user.controller;

import com.docmind.docmind_api.user.dto.UpdatePasswordRequest;
import com.docmind.docmind_api.user.dto.UpdateProfileRequest;
import com.docmind.docmind_api.user.dto.UserProfileResponse;
import com.docmind.docmind_api.user.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping
    public UserProfileResponse getProfile(
            Authentication authentication
    ) {

        return userSettingsService.getProfile(
                authentication.getName()
        );
    }

    @PatchMapping
    public UserProfileResponse updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {

        return userSettingsService.updateProfile(
                authentication.getName(),
                request
        );
    }

    @PutMapping("/password")
    public void updatePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication
    ) {

        userSettingsService.updatePassword(
                authentication.getName(),
                request
        );
    }

    @DeleteMapping
    public void deleteAccount(
            Authentication authentication
    ) {

        userSettingsService.deleteAccount(
                authentication.getName()
        );
    }
}
