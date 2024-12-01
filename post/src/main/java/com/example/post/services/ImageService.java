package com.example.post.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ImageService {

    public String saveImage(MultipartFile image) throws IOException {
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return base64Image;
    }

    public List<String> saveImages(List<MultipartFile> images) throws IOException {
        List<String> imagePaths = new ArrayList<>();
        for (MultipartFile image : images) {
            String imagePath = saveImage(image);
            imagePaths.add(imagePath);
        }
        return imagePaths;
    }
}
