package com.stockanalysis.service;

import com.stockanalysis.model.Candle;
import java.util.List;

public interface MarketDataService {
    List<Candle> getDailyCandles(String symbol, int days);
}

