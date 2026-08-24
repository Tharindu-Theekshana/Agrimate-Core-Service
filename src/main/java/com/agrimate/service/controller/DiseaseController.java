package com.agrimate.service.controller;

import com.agrimate.service.dto.DiseaseDto;
import com.agrimate.service.service.DiseaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public List<DiseaseDto> list() {
        return diseaseService.list();
    }

    @GetMapping("/{key}")
    public DiseaseDto get(@PathVariable String key) {
        return diseaseService.get(key);
    }
}
