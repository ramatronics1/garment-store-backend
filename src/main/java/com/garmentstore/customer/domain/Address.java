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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getFlatHouseNo() { return flatHouseNo; }
    public void setFlatHouseNo(String flatHouseNo) { this.flatHouseNo = flatHouseNo; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getAreaLandmark() { return areaLandmark; }
    public void setAreaLandmark(String areaLandmark) { this.areaLandmark = areaLandmark; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }

    public static AddressBuilder builder() { return new AddressBuilder(); }

    public static class AddressBuilder {
        private Long id;
        private User user;
        private String fullName;
        private String phone;
        private String flatHouseNo;
        private String street;
        private String areaLandmark;
        private String city;
        private String state;
        private String pincode;
        private AddressType addressType;
        private boolean defaultAddress;

        public AddressBuilder id(Long id) { this.id = id; return this; }
        public AddressBuilder user(User user) { this.user = user; return this; }
        public AddressBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public AddressBuilder phone(String phone) { this.phone = phone; return this; }
        public AddressBuilder flatHouseNo(String flatHouseNo) { this.flatHouseNo = flatHouseNo; return this; }
        public AddressBuilder street(String street) { this.street = street; return this; }
        public AddressBuilder areaLandmark(String areaLandmark) { this.areaLandmark = areaLandmark; return this; }
        public AddressBuilder city(String city) { this.city = city; return this; }
        public AddressBuilder state(String state) { this.state = state; return this; }
        public AddressBuilder pincode(String pincode) { this.pincode = pincode; return this; }
        public AddressBuilder addressType(AddressType addressType) { this.addressType = addressType; return this; }
        public AddressBuilder defaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; return this; }

        public Address build() {
            Address a = new Address();
            a.setId(this.id);
            a.setUser(this.user);
            a.setFullName(this.fullName);
            a.setPhone(this.phone);
            a.setFlatHouseNo(this.flatHouseNo);
            a.setStreet(this.street);
            a.setAreaLandmark(this.areaLandmark);
            a.setCity(this.city);
            a.setState(this.state);
            a.setPincode(this.pincode);
            a.setAddressType(this.addressType);
            a.setDefaultAddress(this.defaultAddress);
            return a;
        }
    }
}
