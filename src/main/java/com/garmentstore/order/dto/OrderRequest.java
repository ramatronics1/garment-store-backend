package com.garmentstore.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull(message = "Address ID is required")
    private Long addressId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
