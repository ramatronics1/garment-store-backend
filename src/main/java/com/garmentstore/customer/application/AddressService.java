package com.garmentstore.customer.application;

import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.domain.UserType;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.audit.AuditLogService;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.customer.domain.Address;
import com.garmentstore.customer.dto.AddressRequest;
import com.garmentstore.customer.dto.AddressResponse;
import com.garmentstore.customer.infrastructure.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private static final int MAX = 10;
    private final AddressRepository repo;
    private final UserRepository users;
    private final AuditLogService audit;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long uid) {
        customer(uid);
        return repo.findByUserIdOrderByDefaultAddressDescUpdatedAtDescIdDesc(uid).stream().map(this::resp).toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse get(Long uid, Long id) {
        customer(uid);
        return resp(owned(uid, id));
    }

    @Transactional
    public AddressResponse create(Long uid, AddressRequest r) {
        User u = customer(uid);
        if (repo.countByUserId(uid) >= MAX)
            throw new BusinessException("ADDRESS_LIMIT_EXCEEDED", "Maximum address limit exceeded", HttpStatus.BAD_REQUEST);
        boolean def = Boolean.TRUE.equals(r.defaultAddress()) || repo.countByUserId(uid) == 0;
        if (def) repo.clearDefaultForUser(uid);
        Address a = fill(Address.builder().user(u).build(), r);
        a.setDefaultAddress(def);
        a = repo.save(a);
        audit.record(uid, "CUSTOMER", "CUSTOMER_ADDRESS_CREATED", "ADDRESS", String.valueOf(a.getId()), null, null);
        return resp(a);
    }

    @Transactional
    public AddressResponse update(Long uid, Long id, AddressRequest r) {
        customer(uid);
        Address a = owned(uid, id);
        boolean def = Boolean.TRUE.equals(r.defaultAddress()) || a.isDefaultAddress();
        if (def) repo.clearDefaultForUser(uid);
        a = fill(a, r);
        a.setDefaultAddress(def);
        a = repo.save(a);
        audit.record(uid, "CUSTOMER", "CUSTOMER_ADDRESS_UPDATED", "ADDRESS", String.valueOf(a.getId()), null, null);
        return resp(a);
    }

    @Transactional
    public void delete(Long uid, Long id) {
        customer(uid);
        Address a = owned(uid, id);
        boolean def = a.isDefaultAddress();
        repo.delete(a);
        if (def) repo.findFirstByUserIdAndIdNotOrderByUpdatedAtDescIdDesc(uid, id).ifPresent(n -> {
            n.setDefaultAddress(true);
            repo.save(n);
        });
        audit.record(uid, "CUSTOMER", "CUSTOMER_ADDRESS_DELETED", "ADDRESS", String.valueOf(id), null, null);
    }

    @Transactional
    public AddressResponse markDefault(Long uid, Long id) {
        customer(uid);
        Address a = owned(uid, id);
        repo.clearDefaultForUser(uid);
        a.setDefaultAddress(true);
        return resp(repo.save(a));
    }

    private Address fill(Address a, AddressRequest r) {
        a.setFullName(r.fullName().trim());
        a.setPhone(r.phone().trim());
        a.setFlatHouseNo(r.flatHouseNo().trim());
        a.setStreet(r.street().trim());
        a.setAreaLandmark(blank(r.areaLandmark()));
        a.setCity(r.city().trim());
        a.setState(r.state().trim());
        a.setPincode(r.pincode().trim());
        a.setAddressType(r.addressType());
        return a;
    }

    private Address owned(Long uid, Long id) {
        return repo.findByIdAndUserId(id, uid).orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND));
    }

    private User customer(Long uid) {
        User u = users.findById(uid).orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        if (u.getUserType() != UserType.CUSTOMER)
            throw new BusinessException("CUSTOMER_ACCESS_REQUIRED", "Customer access required", HttpStatus.FORBIDDEN);
        if (u.getAccountStatus() != AccountStatus.ACTIVE)
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Account is not active", HttpStatus.FORBIDDEN);
        return u;
    }

    private AddressResponse resp(Address a) {
        return new AddressResponse(a.getId(), a.getFullName(), a.getPhone(), a.getFlatHouseNo(), a.getStreet(), a.getAreaLandmark(), a.getCity(), a.getState(), a.getPincode(), a.getAddressType(), a.isDefaultAddress(), a.getCreatedAt(), a.getUpdatedAt());
    }

    private String blank(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
