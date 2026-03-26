package com.stockanalysis.model;

import java.util.List;

public record AdviceResult(
        String action,
        int confidence,
        List<String> reasons,
        String riskNote,
        String provider
) {
}

