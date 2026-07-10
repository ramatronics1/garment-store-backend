package com.garmentstore.customer.infrastructure;

import com.garmentstore.customer.domain.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile,Long>{Optional<CustomerProfile> findByUserId(Long userId);}
