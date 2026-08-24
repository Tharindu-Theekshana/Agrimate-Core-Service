package com.agrimate.service.controller;

import com.agrimate.service.dto.CropStageLogDtos.CropStageLogDto;
import com.agrimate.service.dto.CropStageLogDtos.CropStageLogRequest;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.CropStageLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CropStageLogController {

    private final CropStageLogService cropStageLogService;

    public CropStageLogController(CropStageLogService cropStageLogService) {
        this.cropStageLogService = cropStageLogService;
    }

    @GetMapping("/crops/{cropId}/stages")
    public List<CropStageLogDto> list(@AuthenticationPrincipal User user, @PathVariable Long cropId) {
        return cropStageLogService.list(user, cropId);
    }

    @PostMapping("/crops/{cropId}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    public CropStageLogDto create(@AuthenticationPrincipal User user, @PathVariable Long cropId,
                                  @Valid @RequestBody CropStageLogRequest req) {
        return cropStageLogService.create(user, cropId, req);
    }

    @DeleteMapping("/stages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        cropStageLogService.delete(user, id);
    }
}
