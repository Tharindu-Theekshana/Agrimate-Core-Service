package com.agrimate.service.controller;

import com.agrimate.service.dto.CropDtos.CropDto;
import com.agrimate.service.dto.CropDtos.CropRequest;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PatchMapping("/{id}")
    public CropDto update(@AuthenticationPrincipal User user, @PathVariable Long id,
                          @Valid @RequestBody CropRequest req) {
        return cropService.update(user, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        cropService.delete(user, id);
    }
}
