package com.stockanalysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.stockanalysis.model.Candle;
import com.stockanalysis.model.IndicatorSnapshot;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class IndicatorServiceTest {

    @Test
    void shouldCalculateSnapshot() {
        IndicatorService service = new IndicatorService();
        List<Candle> candles = new ArrayList<>();

        double base = 100.0;
        for (int i = 0; i < 60; i++) {
            double close = base + i * 0.8;
            candles.add(new Candle(
                    LocalDate.of(2026, 1, 1).plusDays(i),
                    close - 0.6,
                    close + 1.2,
                    close - 1.5,
                    close,
                    1_000_000 + i * 10_000,
                    close * (1_000_000 + i * 10_000)
            ));
        }

        IndicatorSnapshot snapshot = service.calculate(candles);

        assertNotNull(snapshot);
        assertEquals(candles.get(candles.size() - 1).date().toString(), snapshot.date());
    }
}

