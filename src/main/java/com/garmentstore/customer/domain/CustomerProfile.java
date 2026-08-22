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
@Table(name = "customer_profiles")
public class CustomerProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public static CustomerProfileBuilder builder() { return new CustomerProfileBuilder(); }

    public static class CustomerProfileBuilder {
        private Long id;
        private User user;
        private String firstName;
        private String lastName;
        private String profileImageUrl;

        public CustomerProfileBuilder id(Long id) { this.id = id; return this; }
        public CustomerProfileBuilder user(User user) { this.user = user; return this; }
        public CustomerProfileBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public CustomerProfileBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public CustomerProfileBuilder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }

        public CustomerProfile build() {
            CustomerProfile cp = new CustomerProfile();
            cp.setId(this.id);
            cp.setUser(this.user);
            cp.setFirstName(this.firstName);
            cp.setLastName(this.lastName);
            cp.setProfileImageUrl(this.profileImageUrl);
            return cp;
        }
    }
}
