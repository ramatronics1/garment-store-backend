package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AdminSaleConfigRequest(
        Boolean active,
        @Size(max = 200) String title,
        @Size(max = 400) String subtitle,
        LocalDateTime endsAt,
        @Size(max = 80)  String badgeText,
        @Size(max = 500) String linkUrl,
        @Size(max = 700) String mediaUrl
) {}
