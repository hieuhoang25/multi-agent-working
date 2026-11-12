package com.example.usermanagement.dto;

import com.example.usermanagement.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        User.Role role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.isActive(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
