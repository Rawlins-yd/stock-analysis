package com.stockanalysis.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyAnalysisJob {

    private static final Logger log = LoggerFactory.getLogger(DailyAnalysisJob.class);

    private final AnalysisService analysisService;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${app.scheduler.days:120}")
    private int days;

    public DailyAnalysisJob(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @Scheduled(cron = "${app.scheduler.cron:0 0 18 * * *}")
    public void runDaily() {
        if (!schedulerEnabled) {
            return;
        }
        try {
            List<?> results = analysisService.analyzeWatchlist(days);
            log.info("Daily analysis complete, total symbols: {}", results.size());
        } catch (Exception ex) {
            log.warn("Daily analysis failed: {}", ex.getMessage());
        }
    }
}

