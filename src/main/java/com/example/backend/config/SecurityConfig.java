package com.example.backend.config;

import com.example.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity // @PreAuthorize için
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final UserDetailsService uds;

    public SecurityConfig(JwtAuthFilter jwtFilter, UserDetailsService uds) {
        this.jwtFilter = jwtFilter; this.uds = uds;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Açık uçlar
                        .requestMatchers("/health", "/api/auth/**").permitAll()
                        // Okuma uçlarını açık bırak (React listeleme için)
                        .requestMatchers(GET, "/api/ders/**").permitAll()
                        .requestMatchers(GET, "/api/sorular/**").permitAll()
                        .requestMatchers(GET, "/api/deneme-sinavlari/**").permitAll() // Deneme sınavları okuma
                        .requestMatchers(GET, "/api/deneme-sinavi").permitAll() // Frontend için tekil endpoint (GET - listeleme)
                        .requestMatchers(GET, "/api/deneme-sinavi/**").permitAll() // Frontend için tekil endpoint (GET - detay)
                        .requestMatchers(GET, "/api/raporlar/**").authenticated() // YENİ

                        // Yönetim gerektiren yazma uçları
                        .requestMatchers(POST, "/api/ders/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/sorular/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/api/sorular/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/deneme-sinavlari/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/deneme-sinavi").hasRole("ADMIN") // Frontend için tekil endpoint (POST)
                        .requestMatchers(PUT, "/api/deneme-sinavlari/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/api/deneme-sinavlari/**").hasRole("ADMIN")

                        // Kullanıcı uçları
                        .requestMatchers(GET, "/api/users/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/files/upload").hasRole("ADMIN")
                        .requestMatchers("/files/**").permitAll()                  // statik dosyalar
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/files/upload").hasRole("ADMIN")


                        // Geri kalan her şey auth ister
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authProvider(UserDetailsService uds) {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173","http://localhost:3000"));
        c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        var s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}
