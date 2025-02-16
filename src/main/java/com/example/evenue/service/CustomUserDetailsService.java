package com.example.evenue.service;

import com.example.evenue.models.users.Role;
import com.example.evenue.models.users.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        // Retrieve the user from the database using the email
        UserModel user = userService.findUserByEmail(email.trim());
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Check if the role is null and handle appropriately
        Role role = user.getRole();
        if (role == null) {
            System.out.println("User role is null. Defaulting to ROLE_GUEST.");
            role = Role.GUEST; // Assuming you have a GUEST role as default
        }

        // Log the user and role information
        System.out.println("User found: " + user.getEmail() + ", Role: " + role.name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthorities(role)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        // Convert Role enum to a string representation for the authority
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
