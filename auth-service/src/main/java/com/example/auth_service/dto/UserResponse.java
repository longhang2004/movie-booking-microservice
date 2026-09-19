package com.example.auth_service.dto;

import com.example.auth_service.model.Role;
import com.example.auth_service.model.UserAccount;

public record UserResponse(Long id, String email, String fullName, Role role) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
