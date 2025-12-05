package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 60)
    private String ad;

    @Column(nullable = false, length = 60)
    private String soyad;

    @Column(nullable = false, length = 200)
    private String password; // BCrypt hash

    @Column(nullable = false, length = 30)
    private String role = "USER"; // USER / ADMIN

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 500)
    private String avatarUrl;

    @Column(name = "hedef_siralama")
    private Integer hedefSiralama;

    // getters/setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setAd(String ad) { this.ad = ad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Integer getHedefSiralama() { return hedefSiralama; }
    public void setHedefSiralama(Integer hedefSiralama) { this.hedefSiralama = hedefSiralama; }
}
