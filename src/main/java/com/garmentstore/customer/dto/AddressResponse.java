package com.garmentstore.customer.dto;

import com.garmentstore.customer.domain.AddressType;

import java.time.Instant;

public record AddressResponse(Long id,String fullName,String phone,String flatHouseNo,String street,String areaLandmark,String city,String state,String pincode,AddressType addressType,boolean defaultAddress,Instant createdAt,Instant updatedAt){}
