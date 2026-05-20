package com.example.backend.config;

import com.example.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
                        // Tarayıcı CORS preflight — JWT yok; burada takılırsa header gelmez
                        .requestMatchers(OPTIONS, "/**").permitAll()
                        // Açık uçlar
                        .requestMatchers("/health", "/uptime", "/api/auth/**").permitAll()
                        // Okuma uçlarını açık bırak (React listeleme için)
                        .requestMatchers(GET, "/api/ders/**").permitAll()
                        .requestMatchers(GET, "/api/sorular/**").permitAll()
                        .requestMatchers(GET, "/api/konu/**").permitAll() // Konu listeleme
                        .requestMatchers(GET, "/api/konular/**").permitAll() // Konu listeleme (çoğul)
                        .requestMatchers(GET, "/api/deneme-sinavlari/**").permitAll() // Deneme sınavları okuma
                        .requestMatchers(GET, "/api/deneme-sinavi").permitAll() // Frontend için tekil endpoint (GET - listeleme)
                        .requestMatchers(GET, "/api/deneme-sinavi/**").permitAll() // Frontend için tekil endpoint (GET - detay)
                        .requestMatchers(GET, "/api/raporlar/**").authenticated() // Raporlar ve günlük performans grafiği

                        // Yönetim gerektiren yazma uçları
                        .requestMatchers(POST, "/api/ders/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/sorular/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/api/sorular/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/konu/**").hasRole("ADMIN") // Konu oluşturma
                        .requestMatchers(POST, "/api/konular/**").hasRole("ADMIN") // Konu oluşturma (çoğul)
                        .requestMatchers(PUT, "/api/konu/**").hasRole("ADMIN") // Konu güncelleme
                        .requestMatchers(PUT, "/api/konular/**").hasRole("ADMIN") // Konu güncelleme (çoğul)
                        .requestMatchers(DELETE, "/api/konu/**").hasRole("ADMIN") // Konu silme
                        .requestMatchers(DELETE, "/api/konular/**").hasRole("ADMIN") // Konu silme (çoğul)
                        .requestMatchers(POST, "/api/deneme-sinavlari/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/deneme-sinavi").hasRole("ADMIN") // Frontend için tekil endpoint (POST)
                        .requestMatchers(PUT, "/api/deneme-sinavlari/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/api/deneme-sinavlari/**").hasRole("ADMIN")

                        // Kullanıcı uçları
                        .requestMatchers(GET, "/api/users/me").authenticated()
                        .requestMatchers(GET, "/api/users/me/solved-questions/**").authenticated()
                        .requestMatchers(GET, "/api/activities/**").authenticated() // Son aktiviteler
                        .requestMatchers(POST, "/api/activities/**").authenticated() // Aktivite kaydet
                        .requestMatchers("/api/video-notes/**").authenticated() // Video notları (CRUD)
                        .requestMatchers(POST, "/api/quiz/**").authenticated() // Quiz submit
                        .requestMatchers(POST, "/api/cevap/**").authenticated() // Cevap submit (deneme sınavı)
                        // Deneme sınavı submit endpoint'leri (frontend) - pattern'leri düzeltildi
                        .requestMatchers(POST, "/api/deneme-sinavi/*/submit").authenticated()
                        .requestMatchers(POST, "/api/deneme-sinavi/*/cevap").authenticated()
                        .requestMatchers(POST, "/api/deneme-sinavi/*/cevaplar").authenticated()
                        .requestMatchers(POST, "/api/deneme-sinavlari/*/submit").authenticated() // Deneme sınavı submit (backend)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/files/**").permitAll()                  // statik dosyalar (GET) - önce kontrol et
                        .requestMatchers("/api/files/**").hasRole("ADMIN") // Tüm file işlemleri ADMIN için


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
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ALLOWED_ORIGINS:}") String extraOriginsCsv
    ) {
        var c = new CorsConfiguration();
        /*
         * credentials=true ile wildcard origin kullanılamaz; pattern kullanılır.
         * Vercel preview/production: https://*.vercel.app
         */
        Set<String> patterns = new LinkedHashSet<>(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                /* Tek segment: foo.vercel.app */
                "https://*.vercel.app",
                /* Vercel preview (deployment-hash-team...) iç içe alt alan adları */
                "https://*.*.vercel.app",
                "https://*.*.*.vercel.app"
        ));
        if (extraOriginsCsv != null && !extraOriginsCsv.isBlank()) {
            for (String part : extraOriginsCsv.split(",")) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    patterns.add(t);
                }
            }
        }
        c.setAllowedOriginPatterns(new ArrayList<>(patterns));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        c.setMaxAge(3600L);
        var s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}
