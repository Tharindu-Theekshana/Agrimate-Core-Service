package com.agrimate.service.controller;

import com.agrimate.service.dto.ScanDto;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.ScanService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping(value = "/guest", consumes = "multipart/form-data")
    public ScanDto guestScan(@RequestParam("image") MultipartFile image) {
        return scanService.scanGuest(image);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ScanDto scan(@AuthenticationPrincipal User user,
                        @RequestParam("image") MultipartFile image,
                        @RequestParam(value = "farmId", required = false) Long farmId,
                        @RequestParam(value = "cropId", required = false) Long cropId,
                        @RequestParam(value = "latitude", required = false) Double latitude,
                        @RequestParam(value = "longitude", required = false) Double longitude) {
        return scanService.scan(user, image, farmId, cropId, latitude, longitude);
    }

    @GetMapping
    public Page<ScanDto> history(@AuthenticationPrincipal User user,
                                 @RequestParam(value = "disease", required = false) String disease,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "20") int size) {
        return scanService.history(user, disease, page, size);
    }

    @GetMapping("/{id}")
    public ScanDto get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return scanService.get(user, id);
    }
}
