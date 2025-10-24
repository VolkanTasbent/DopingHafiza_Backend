package com.example.backend.service;

import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository repo;

    public AppUserDetailsService(AppUserRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Kullanıcı yok"));
        return User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole()) // "USER" -> ROLE_USER
                .disabled(!u.isEnabled())
                .build();
    }
}
