package com.han.bloomi.api.controller;

import com.han.bloomi.api.dto.AnalyzeMealRequest;
import com.han.bloomi.api.dto.AnalyzeMealResponse;
import com.han.bloomi.application.service.MealAnalyzeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/meal")
@RequiredArgsConstructor
public class MealAnalyzeController {
    private final MealAnalyzeService service;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeMealResponse analyze(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "weight", required = false) Double weight,
            @RequestParam(value = "notes", required = false) String notes
    ) {
        log.info("Received analyze request - image: {}, name: {}, weight: {}",
                image.getOriginalFilename(), name, weight);

        AnalyzeMealRequest request = new AnalyzeMealRequest(name, weight, notes);
        return service.analyze(image, request);
    }
}