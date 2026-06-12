package com.docmind.docmind_api.user.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {

    private String currentPassword;

    private String newPassword;
}
