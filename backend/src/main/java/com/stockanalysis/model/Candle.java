package com.stockanalysis.model;

import java.time.LocalDate;

public record Candle(
        LocalDate date,
        double open,
        double high,
        double low,
        double close,
        double volume,
        double turnover
) {
}

