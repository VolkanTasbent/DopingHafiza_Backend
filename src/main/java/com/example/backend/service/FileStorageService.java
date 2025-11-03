package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root = Paths.get("./uploads").toAbsolutePath().normalize();
    private final Path avatarRoot = root.resolve("avatars");
    private final Path docsRoot = root.resolve("docs");
    private final Path videosRoot = root.resolve("videos");

    public FileStorageService() throws IOException {
        Files.createDirectories(root);
        Files.createDirectories(avatarRoot);
        Files.createDirectories(docsRoot);
        Files.createDirectories(videosRoot);
    }

    /**
     * Genel dosya kaydetme
     */
    public String save(MultipartFile file) throws IOException {
        return save(file, "general");
    }

    /**
     * Genel dosya kaydetme (folder parametreli)
     */
    public String save(MultipartFile file, String folder) throws IOException {
        Path targetDir = root.resolve(folder);
        Files.createDirectories(targetDir);
        
        String ext = getFileExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = targetDir.resolve(name);
        
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        return "/files/" + folder + "/" + name;
    }

    /**
     * Profil resmi kaydetme (kullanıcıya özel)
     */
    public String saveAvatar(MultipartFile file, String username) throws IOException {
        String ext = getFileExtension(file.getOriginalFilename());
        
        // Kullanıcı adı + timestamp ile benzersiz dosya adı
        String name = sanitizeUsername(username) + "_" + System.currentTimeMillis() + ext;
        Path target = avatarRoot.resolve(name);
        
        // Eski profil resimlerini temizle (opsiyonel)
        deleteOldAvatars(username);
        
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        return "/files/avatars/" + name;
    }

    /**
     * Dosya uzantısını al
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return (i >= 0) ? filename.substring(i) : "";
    }

    /**
     * Username'i dosya adı için güvenli hale getir
     */
    private String sanitizeUsername(String username) {
        return username.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Kullanıcının eski profil resimlerini sil
     */
    private void deleteOldAvatars(String username) {
        try {
            String sanitized = sanitizeUsername(username);
            Files.list(avatarRoot)
                .filter(path -> path.getFileName().toString().startsWith(sanitized + "_"))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Log the error but don't fail the upload
                        System.err.println("Could not delete old avatar: " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            // Log the error but don't fail the upload
            System.err.println("Could not list old avatars: " + e.getMessage());
        }
    }

    /**
     * Döküman kaydetme (PDF vb.)
     */
    public String saveDokuman(MultipartFile file) throws IOException {
        String ext = getFileExtension(file.getOriginalFilename());
        
        // Dosya adını temizle ve timestamp ekle
        String original = file.getOriginalFilename();
        String baseName = "dokuman";
        if (original != null && original.lastIndexOf('.') > 0) {
            baseName = original.substring(0, original.lastIndexOf('.'))
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
        }
        String name = baseName + "_" + System.currentTimeMillis() + ext;
        
        Path target = docsRoot.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        return "/files/docs/" + name;
    }

    /**
     * Video kaydetme (MP4, MOV vb.)
     */
    public String saveVideo(MultipartFile file) throws IOException {
        String ext = getFileExtension(file.getOriginalFilename());
        
        // Dosya adını temizle ve timestamp ekle
        String original = file.getOriginalFilename();
        String baseName = "video";
        if (original != null && original.lastIndexOf('.') > 0) {
            baseName = original.substring(0, original.lastIndexOf('.'))
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
        }
        String name = baseName + "_" + System.currentTimeMillis() + ext;
        
        Path target = videosRoot.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        return "/files/videos/" + name;
    }
}
