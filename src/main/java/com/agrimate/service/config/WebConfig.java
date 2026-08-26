package com.agrimate.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/** Serves locally-stored scan images (fallback when Cloudinary is not configured). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String localDir;

    public WebConfig(@Value("${agrimate.storage.local-dir:uploads}") String localDir) {
        this.localDir = localDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(localDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
