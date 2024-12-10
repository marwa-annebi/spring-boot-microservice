package com.example.post.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageUploadService {

    private static final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    public String saveImage(MultipartFile file) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG files are allowed");
        }

        // Validate file size (e.g., 5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the 5MB limit");
        }

        // Save file
        String fileName = file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIRECTORY, fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        return fileName;
    }
}
