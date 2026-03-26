package com.stockanalysis.service;

import com.stockanalysis.model.AdviceResult;
import com.stockanalysis.model.Candle;
import com.stockanalysis.model.IndicatorSnapshot;
import com.stockanalysis.model.RuntimeConfig;
import com.stockanalysis.model.StockAnalysisResult;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final MarketDataService marketDataService;
    private final IndicatorService indicatorService;
    private final LlmAdvisoryService llmAdvisoryService;
    private final ConfigService configService;
    private final Map<String, StockAnalysisResult> latestResults = new ConcurrentHashMap<>();

    public AnalysisService(
            MarketDataService marketDataService,
            IndicatorService indicatorService,
            LlmAdvisoryService llmAdvisoryService,
            ConfigService configService
    ) {
        this.marketDataService = marketDataService;
        this.indicatorService = indicatorService;
        this.llmAdvisoryService = llmAdvisoryService;
        this.configService = configService;
    }

    public StockAnalysisResult analyze(String symbol, int days) {
        RuntimeConfig config = configService.getConfig();
        List<Candle> candles = marketDataService.getDailyCandles(symbol, days);
        IndicatorSnapshot indicators = indicatorService.calculate(candles);
        AdviceResult advice = llmAdvisoryService.advise(symbol, indicators, config);

        StockAnalysisResult result = new StockAnalysisResult(
                symbol.toUpperCase(),
                OffsetDateTime.now().toString(),
                candles,
                indicators,
                advice
        );
        latestResults.put(symbol.toUpperCase(), result);
        return result;
    }

    public List<StockAnalysisResult> analyzeWatchlist(int days) {
        List<StockAnalysisResult> results = new ArrayList<>();
        for (String symbol : configService.symbols()) {
            results.add(analyze(symbol, days));
        }
        return results;
    }

    public List<StockAnalysisResult> latestResults() {
        return new ArrayList<>(latestResults.values());
    }
}

