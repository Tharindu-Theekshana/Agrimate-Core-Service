package com.agrimate.service.controller;

import com.agrimate.service.dto.CropDtos.CropDto;
import com.agrimate.service.dto.CropDtos.CropRequest;
import com.agrimate.service.dto.FarmDtos.FarmDto;
import com.agrimate.service.dto.FarmDtos.FarmRequest;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.CropService;
import com.agrimate.service.service.FarmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;
    private final CropService cropService;

    public FarmController(FarmService farmService, CropService cropService) {
        this.farmService = farmService;
        this.cropService = cropService;
    }

    @GetMapping
    public List<FarmDto> list(@AuthenticationPrincipal User user) {
        return farmService.list(user);
    }

    @GetMapping("/{id}")
    public FarmDto get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return farmService.get(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FarmDto create(@AuthenticationPrincipal User user, @Valid @RequestBody FarmRequest req) {
        return farmService.create(user, req);
    }

    @PatchMapping("/{id}")
    public FarmDto update(@AuthenticationPrincipal User user, @PathVariable Long id,
                          @Valid @RequestBody FarmRequest req) {
        return farmService.update(user, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        farmService.delete(user, id);
    }

    @GetMapping("/{id}/crops")
    public List<CropDto> crops(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return cropService.listByFarm(user, id);
    }

    @PostMapping("/{id}/crops")
    @ResponseStatus(HttpStatus.CREATED)
    public CropDto addCrop(@AuthenticationPrincipal User user, @PathVariable Long id,
                           @Valid @RequestBody CropRequest req) {
        return cropService.create(user, id, req);
    }
}
