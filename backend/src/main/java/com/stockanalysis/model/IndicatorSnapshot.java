package com.stockanalysis.model;

public record IndicatorSnapshot(
        String date,
        double close,
        double priceChangePct,
        double macdDif,
        double macdDea,
        double macdHist,
        String macdSignal,
        double k,
        double d,
        double j,
        String kdjSignal,
        double volume,
        double volumeRatio,
        double turnover
) {
}

