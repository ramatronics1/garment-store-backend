package com.garmentstore.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(@NotBlank @Size(min = 2, max = 100) String firstName,
                                   @Size(max = 100) String lastName, @Size(max = 500) String profileImageUrl) {
}
