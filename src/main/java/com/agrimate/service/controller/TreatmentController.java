package com.agrimate.service.controller;

import com.agrimate.service.dto.TreatmentDtos.TreatmentDto;
import com.agrimate.service.dto.TreatmentDtos.TreatmentRequest;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.TreatmentService;
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
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping("/crops/{cropId}/treatments")
    public List<TreatmentDto> list(@AuthenticationPrincipal User user, @PathVariable Long cropId) {
        return treatmentService.list(user, cropId);
    }

    @PostMapping("/crops/{cropId}/treatments")
    @ResponseStatus(HttpStatus.CREATED)
    public TreatmentDto create(@AuthenticationPrincipal User user, @PathVariable Long cropId,
                               @Valid @RequestBody TreatmentRequest req) {
        return treatmentService.create(user, cropId, req);
    }

    @DeleteMapping("/treatments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        treatmentService.delete(user, id);
    }
}
