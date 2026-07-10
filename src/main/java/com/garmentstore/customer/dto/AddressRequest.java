package com.garmentstore.customer.dto;

import com.garmentstore.customer.domain.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(@NotBlank@Size(min=2,max=150)String fullName,@NotBlank@Pattern(regexp="^[6-9][0-9]{9}$")String phone,@NotBlank@Size(max=150)String flatHouseNo,@NotBlank@Size(max=200)String street,@Size(max=200)String areaLandmark,@NotBlank@Size(max=100)String city,@NotBlank@Size(max=100)String state,@NotBlank@Pattern(regexp="^[1-9][0-9]{5}$")String pincode,@NotNull AddressType addressType,Boolean defaultAddress){}
