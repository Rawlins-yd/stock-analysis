package com.stockanalysis.model;

import java.util.List;

public record StockAnalysisResult(
        String symbol,
        String generatedAt,
        List<Candle> candles,
        IndicatorSnapshot indicators,
        AdviceResult advice
) {
}

