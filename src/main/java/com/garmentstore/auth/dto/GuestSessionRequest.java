package com.garmentstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GuestSessionRequest(
        @Email(message = "Email must be valid")
        @Size(max = 180, message = "Email must not exceed 180 characters")
        String email,

        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile must be a valid 10 digit Indian mobile number")
        String mobile
) {}
