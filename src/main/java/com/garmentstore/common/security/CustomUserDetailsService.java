package com.garmentstore.common.security;

import com.garmentstore.auth.domain.*;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByEmailIgnoreCase(username).or(() -> userRepository.findByMobile(username)).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toUserDetails(u);
    }

    public UserDetails loadById(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toUserDetails(u);
    }

    private UserDetails toUserDetails(User u) {
        Collection<? extends GrantedAuthority> auth = u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getCode())).toList();
        boolean enabled = u.getAccountStatus() == AccountStatus.ACTIVE || u.getAccountStatus() == AccountStatus.PENDING_VERIFICATION;
        return org.springframework.security.core.userdetails.User.withUsername(String.valueOf(u.getId())).password(u.getPasswordHash()).authorities(auth).accountLocked(u.getAccountStatus() == AccountStatus.LOCKED).disabled(!enabled).build();
    }
}
