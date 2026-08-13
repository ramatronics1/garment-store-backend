package com.garmentstore.order.dto;

import lombok.Builder;
import lombok.Data;

/** Snapshot of the delivery address used in the Admin order detail. */
@Data
@Builder
public class AdminDeliveryAddressResponse {
    private Long addressId;
    private String fullName;
    private String phone;
    private String flatHouseNo;
    private String street;
    private String areaLandmark;
    private String city;
    private String state;
    private String pincode;
    private String addressType;
}
