package com.stockanalysis.controller;

import com.stockanalysis.model.AnalysisRequest;
import com.stockanalysis.model.StockAnalysisResult;
import com.stockanalysis.service.AnalysisService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/api/analyze")
    public ResponseEntity<StockAnalysisResult> analyze(@Valid @RequestBody AnalysisRequest request) {
        return ResponseEntity.ok(analysisService.analyze(request.getSymbol(), request.getDays()));
    }

    @PostMapping("/api/analyze/watchlist")
    public ResponseEntity<List<StockAnalysisResult>> analyzeWatchlist(@RequestParam(defaultValue = "120") int days) {
        return ResponseEntity.ok(analysisService.analyzeWatchlist(days));
    }

    @GetMapping("/api/analysis/latest")
    public ResponseEntity<List<StockAnalysisResult>> latest() {
        return ResponseEntity.ok(analysisService.latestResults());
    }
}

