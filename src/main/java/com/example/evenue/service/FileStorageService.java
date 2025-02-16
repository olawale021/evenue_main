package com.example.evenue.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class FileStorageService {

    /**
     * This method handles the file upload and converts the image to a base64-encoded string,
     * which is returned to be stored or displayed as needed.
     *
     * @param file the uploaded image file
     * @return the base64-encoded image string with the proper data URI prefix, or null if there's an error
     */
    public String convertToBase64AndStore(MultipartFile file) {
        try {
            // Check if the file is provided and is not empty
            if (file != null && !file.isEmpty()) {

                // Check file size (optional: max 5MB)
                if (file.getSize() > 5 * 1024 * 1024) {
                    throw new IOException("File size exceeds 5MB limit.");
                }

                // Get the file bytes
                byte[] fileBytes = file.getBytes();

                // Convert the file bytes to a Base64-encoded string
                String base64EncodedImage = Base64.getEncoder().encodeToString(fileBytes);

                // Construct the Base64 image string with the data URI prefix
                // Example: "data:image/jpeg;base64,{base64EncodedImage}"
                String base64WithPrefix = "data:" + file.getContentType() + ";base64," + base64EncodedImage;

                // Return the base64 encoded string with the data URI prefix
                return base64WithPrefix;
            } else {
                return null; // Return null if the file is empty or not provided
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to convert file to base64", e);
        }
    }
}

