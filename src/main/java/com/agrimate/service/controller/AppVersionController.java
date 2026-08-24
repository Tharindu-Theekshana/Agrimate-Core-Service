package com.agrimate.service.controller;

import com.agrimate.service.dto.AppVersionDtos.CheckResponse;
import com.agrimate.service.model.appVersion.Platform;
import com.agrimate.service.service.AppVersionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app-version")
public class AppVersionController {

    private final AppVersionService appVersionService;

    public AppVersionController(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;
    }

    @GetMapping("/check")
    public CheckResponse check(@RequestParam Platform platform, @RequestParam String version) {
        return appVersionService.check(platform, version);
    }
}
