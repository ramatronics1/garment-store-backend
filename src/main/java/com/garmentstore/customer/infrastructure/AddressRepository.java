package com.garmentstore.customer.infrastructure;

import com.garmentstore.customer.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByDefaultAddressDescUpdatedAtDescIdDesc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findFirstByUserIdAndIdNotOrderByUpdatedAtDescIdDesc(Long userId, Long excludedAddressId);

    long countByUserId(Long userId);

    @Modifying
    @Query("update Address a set a.defaultAddress=false where a.user.id=:userId")
    int clearDefaultForUser(@Param("userId") Long userId);
}
