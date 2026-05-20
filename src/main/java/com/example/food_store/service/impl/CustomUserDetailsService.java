package com.example.food_store.service.impl;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.food_store.domain.User user = this.userService.getUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        
        return new User(
                user.getEmail(),
                user.getPassword(),
                // CHỖ SỬA: Truyền trực tiếp user.getRole().getName() (đã có sẵn chữ ROLE_ADMIN từ DB)
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName()))
        );
    }
}