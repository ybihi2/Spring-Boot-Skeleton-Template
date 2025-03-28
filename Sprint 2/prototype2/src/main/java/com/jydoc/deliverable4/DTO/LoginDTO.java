package com.jydoc.deliverable4.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotBlank(message = "Username or email is required")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
    public static LoginDTO empty() {
        return new LoginDTO("", "");
    }

    public String getNormalizedUsername() {
        return username.trim().toLowerCase();
    }
}