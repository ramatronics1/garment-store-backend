package com.garmentstore.customer.domain;

import com.garmentstore.auth.domain.User;
import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;
    @Column(nullable = false, length = 20)
    private String phone;
    @Column(name = "flat_house_no", nullable = false, length = 150)
    private String flatHouseNo;
    @Column(nullable = false, length = 200)
    private String street;
    @Column(name = "area_landmark", length = 200)
    private String areaLandmark;
    @Column(nullable = false, length = 100)
    private String city;
    @Column(nullable = false, length = 100)
    private String state;
    @Column(nullable = false, length = 10)
    private String pincode;
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 30)
    private AddressType addressType;
    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;
}
