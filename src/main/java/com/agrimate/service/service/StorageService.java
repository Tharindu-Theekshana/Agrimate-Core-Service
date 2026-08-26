package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final Cloudinary cloudinary;
    private final boolean cloudinaryEnabled;
    private final Path localDir;
    private final String publicBaseUrl;

    public StorageService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${agrimate.storage.local-dir:uploads}") String localDirPath,
            @Value("${agrimate.storage.public-base-url:http://localhost:8080}") String publicBaseUrl) {

        this.localDir = Paths.get(localDirPath).toAbsolutePath();
        this.publicBaseUrl = publicBaseUrl;

        boolean credentialsProvided = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
        Cloudinary configured = null;
        boolean connected = false;

        if (credentialsProvided) {
            configured = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret, "secure", true));
            try {
                configured.api().ping(ObjectUtils.emptyMap());
                connected = true;
                log.info("Cloudinary connected successfully (cloud_name='{}').", cloudName);
            } catch (Exception e) {
                log.warn("Cloudinary credentials configured but the connection check failed "
                        + "(cloud_name='{}') — falling back to local storage at '{}'. Reason: {}",
                        cloudName, localDir, e.getMessage());
            }
        } else {
            log.warn("Cloudinary not configured — storing scan images locally at '{}'. "
                    + "Set CLOUDINARY_* env vars for production.", localDir);
        }

        this.cloudinaryEnabled = connected;
        this.cloudinary = connected ? configured : null;
    }

    public String upload(MultipartFile file) {
        try {
            return cloudinaryEnabled ? uploadToCloudinary(file) : uploadLocally(file);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String uploadToCloudinary(MultipartFile file) throws IOException {
        var result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "agrimate/scans", "resource_type", "image"));
        return (String) result.get("secure_url");
    }

    private String uploadLocally(MultipartFile file) throws IOException {
        Files.createDirectories(localDir);
        String ext = extensionOf(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.write(localDir.resolve(filename), file.getBytes());
        return "/uploads/" + filename;
    }

    private String extensionOf(String name) {
        if (name == null) return ".jpg";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".jpg";
    }
}
