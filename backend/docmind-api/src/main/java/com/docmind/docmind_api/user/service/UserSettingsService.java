package com.docmind.docmind_api.user.service;

import com.docmind.docmind_api.auth.entity.User;
import com.docmind.docmind_api.auth.repository.UserRepository;
import com.docmind.docmind_api.user.dto.UpdatePasswordRequest;
import com.docmind.docmind_api.user.dto.UpdateProfileRequest;
import com.docmind.docmind_api.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(
            String email
    ) {

        return toResponse(
                getUser(email)
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(
            String email,
            UpdateProfileRequest request
    ) {

        User user =
                getUser(email);

        user.setFullName(
                normalizeName(
                        request.getFullName()
                )
        );

        return toResponse(
                userRepository.save(user)
        );
    }

    @Transactional
    public void updatePassword(
            String email,
            UpdatePasswordRequest request
    ) {

        User user =
                getUser(email);

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash()
        )) {
            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()
                || request.getNewPassword().length() < 8) {
            throw new RuntimeException(
                    "New password must be at least 8 characters"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    private User getUser(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }

    private UserProfileResponse toResponse(
            User user
    ) {

        return new UserProfileResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getFullName()
        );
    }

    private String normalizeName(
            String fullName
    ) {

        if (fullName == null || fullName.isBlank()) {
            return null;
        }

        return fullName.trim();
    }
}
