package com.example.usermanagement.dto;

import com.example.usermanagement.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "fullName is required")
        @Size(max = 100, message = "fullName must be at most 100 characters")
        String fullName,

        @NotBlank(message = "email is required")
        @Email(message = "email is invalid")
        @Size(max = 150)
        String email,

        @Pattern(regexp = "^$|^\\+?[0-9]{9,15}$", message = "phone is invalid")
        String phone,

        User.Role role,

        Boolean active
) {
}
