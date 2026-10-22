package com.hrgenius.service;

import com.hrgenius.model.EmployeeStatus;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

// Loads user credentials and permissions for Spring Security authentication.
// Automatically deactivates authentication for employees whose status is INACTIVE.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Enforce account deactivation: if linked employee is INACTIVE, disable login
        boolean enabled = true;
        if (user.getRole() == Role.EMPLOYEE && user.getEmployee() != null) {
            if (user.getEmployee().getStatus() == EmployeeStatus.INACTIVE) {
                enabled = false;
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled,
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
