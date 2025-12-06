package com.example.backend.dto;

public class UpdateUserDTO {
    private String ad;
    private String soyad;
    private String email;
    private String avatarUrl;
    private Integer hedefSiralama;
    private String hedefUniversite;
    private String hedefBolum;
    private java.math.BigDecimal hedefPuan;
    private Boolean darkMode;

    // Getters ve Setters
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Integer getHedefSiralama() { return hedefSiralama; }
    public void setHedefSiralama(Integer hedefSiralama) { this.hedefSiralama = hedefSiralama; }

    public String getHedefUniversite() { return hedefUniversite; }
    public void setHedefUniversite(String hedefUniversite) { this.hedefUniversite = hedefUniversite; }

    public String getHedefBolum() { return hedefBolum; }
    public void setHedefBolum(String hedefBolum) { this.hedefBolum = hedefBolum; }

    public java.math.BigDecimal getHedefPuan() { return hedefPuan; }
    public void setHedefPuan(java.math.BigDecimal hedefPuan) { this.hedefPuan = hedefPuan; }

    public Boolean getDarkMode() { return darkMode; }
    public void setDarkMode(Boolean darkMode) { this.darkMode = darkMode; }
}





