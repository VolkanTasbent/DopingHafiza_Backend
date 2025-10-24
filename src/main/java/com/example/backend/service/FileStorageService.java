package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root = Paths.get("./uploads").toAbsolutePath().normalize();

    public FileStorageService() throws IOException {
        Files.createDirectories(root);
    }

    public String save(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null) {
            int i = original.lastIndexOf('.');
            if (i >= 0) ext = original.substring(i);
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = root.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        // Sunulacak URL
        return "/files/" + name;
    }
}
