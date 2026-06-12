package com.docmind.docmind_api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {

    private String id;

    private String email;

    private String fullName;
}
