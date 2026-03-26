package com.stockanalysis.controller;

import com.stockanalysis.model.RuntimeConfig;
import com.stockanalysis.service.ConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/api/config")
    public ResponseEntity<RuntimeConfig> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }

    @PostMapping("/api/config")
    public ResponseEntity<RuntimeConfig> updateConfig(@Valid @RequestBody RuntimeConfig config) {
        return ResponseEntity.ok(configService.update(config));
    }
}

