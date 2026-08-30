package com.agrimate.service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService serviceWithoutCloudinary() {
        return new StorageService("", "", "", tempDir.toString(), "http://localhost:8080");
    }

    // BE-STORE-01
    @Test
    void upload_withoutCloudinaryCredentials_writesTheFileLocallyAndReturnsARelativeUrl() throws Exception {
        StorageService storage = serviceWithoutCloudinary();
        var file = new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String url = storage.upload(file);

        assertThat(url).startsWith("/uploads/").endsWith(".jpg");
        String filename = url.substring("/uploads/".length());
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
        assertThat(Files.readAllBytes(tempDir.resolve(filename))).containsExactly(1, 2, 3);
    }

    // BE-STORE-02
    @Test
    void upload_defaultsToJpgExtension_whenTheOriginalFilenameHasNone() {
        StorageService storage = serviceWithoutCloudinary();
        var file = new MockMultipartFile("image", "leaf-no-extension", "image/jpeg", new byte[]{1});

        String url = storage.upload(file);

        assertThat(url).endsWith(".jpg");
    }

    // BE-STORE-03
    @Test
    void upload_preservesTheOriginalFileExtension() {
        StorageService storage = serviceWithoutCloudinary();
        var file = new MockMultipartFile("image", "leaf.png", "image/png", new byte[]{1});

        String url = storage.upload(file);

        assertThat(url).endsWith(".png");
    }

    // BE-STORE-04
    @Test
    void upload_generatesAUniqueFilenamePerCall_evenForTheSameOriginalName() {
        StorageService storage = serviceWithoutCloudinary();
        var file1 = new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{1});
        var file2 = new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{2});

        String url1 = storage.upload(file1);
        String url2 = storage.upload(file2);

        assertThat(url1).isNotEqualTo(url2);
    }
}
