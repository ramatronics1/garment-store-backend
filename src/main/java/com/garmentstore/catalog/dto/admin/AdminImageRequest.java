package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminImageRequest(@NotBlank@Size(max=700)String mediaUrl, Integer displayOrder, Boolean thumbnail){}