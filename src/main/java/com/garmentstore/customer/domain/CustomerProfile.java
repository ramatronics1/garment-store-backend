package com.garmentstore.customer.domain;

import com.garmentstore.auth.domain.User;
import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
@Getter@Setter@Builder@NoArgsConstructor@AllArgsConstructor@Entity@Table(name="customer_profiles") public class CustomerProfile extends BaseEntity{@Id@GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@OneToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="user_id",nullable=false,unique=true)private User user;@Column(name="first_name",nullable=false,length=100)private String firstName;@Column(name="last_name",length=100)private String lastName;@Column(name="profile_image_url",length=500)private String profileImageUrl;}
