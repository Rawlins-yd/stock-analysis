package com.stockanalysis.service;

import com.stockanalysis.model.Candle;
import com.stockanalysis.model.IndicatorSnapshot;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IndicatorService {

    public IndicatorSnapshot calculate(List<Candle> candles) {
        int n = candles.size();
        if (n < 35) {
            throw new IllegalArgumentException("Need at least 35 candles to calculate indicators.");
        }

        double[] closes = candles.stream().mapToDouble(Candle::close).toArray();
        double[] highs = candles.stream().mapToDouble(Candle::high).toArray();
        double[] lows = candles.stream().mapToDouble(Candle::low).toArray();
        double[] volumes = candles.stream().mapToDouble(Candle::volume).toArray();

        Macd macd = calculateMacd(closes);
        Kdj kdj = calculateKdj(closes, highs, lows);

        int idx = n - 1;
        int prev = n - 2;
        double priceChangePct = (closes[idx] - closes[prev]) / closes[prev] * 100.0;

        String macdSignal = "neutral";
        if (macd.dif[prev] <= macd.dea[prev] && macd.dif[idx] > macd.dea[idx]) {
            macdSignal = "golden";
        } else if (macd.dif[prev] >= macd.dea[prev] && macd.dif[idx] < macd.dea[idx]) {
            macdSignal = "dead";
        }

        String kdjSignal = "neutral";
        if (kdj.k[prev] <= kdj.d[prev] && kdj.k[idx] > kdj.d[idx]) {
            kdjSignal = "golden";
        } else if (kdj.k[prev] >= kdj.d[prev] && kdj.k[idx] < kdj.d[idx]) {
            kdjSignal = "dead";
        }

        double volumeRatio = calculateVolumeRatio(volumes, idx);
        Candle latest = candles.get(idx);

        return new IndicatorSnapshot(
                latest.date().toString(),
                latest.close(),
                priceChangePct,
                macd.dif[idx],
                macd.dea[idx],
                macd.hist[idx],
                macdSignal,
                kdj.k[idx],
                kdj.d[idx],
                kdj.j[idx],
                kdjSignal,
                latest.volume(),
                volumeRatio,
                latest.turnover()
        );
    }

    private Macd calculateMacd(double[] closes) {
        double[] ema12 = ema(closes, 12);
        double[] ema26 = ema(closes, 26);
        double[] dif = new double[closes.length];
        for (int i = 0; i < closes.length; i++) {
            dif[i] = ema12[i] - ema26[i];
        }
        double[] dea = ema(dif, 9);
        double[] hist = new double[closes.length];
        for (int i = 0; i < closes.length; i++) {
            hist[i] = (dif[i] - dea[i]) * 2;
        }
        return new Macd(dif, dea, hist);
    }

    private Kdj calculateKdj(double[] closes, double[] highs, double[] lows) {
        int n = closes.length;
        double[] rsv = new double[n];
        for (int i = 0; i < n; i++) {
            int start = Math.max(0, i - 8);
            double highest = highs[start];
            double lowest = lows[start];
            for (int j = start + 1; j <= i; j++) {
                highest = Math.max(highest, highs[j]);
                lowest = Math.min(lowest, lows[j]);
            }
            double denominator = highest - lowest;
            rsv[i] = denominator == 0 ? 50.0 : (closes[i] - lowest) / denominator * 100.0;
        }

        double[] k = new double[n];
        double[] d = new double[n];
        double[] j = new double[n];
        k[0] = 50;
        d[0] = 50;
        j[0] = 50;

        for (int i = 1; i < n; i++) {
            k[i] = (2.0 / 3.0) * k[i - 1] + (1.0 / 3.0) * rsv[i];
            d[i] = (2.0 / 3.0) * d[i - 1] + (1.0 / 3.0) * k[i];
            j[i] = 3 * k[i] - 2 * d[i];
        }
        return new Kdj(k, d, j);
    }

    private double[] ema(double[] input, int period) {
        double[] out = new double[input.length];
        out[0] = input[0];
        double alpha = 2.0 / (period + 1);
        for (int i = 1; i < input.length; i++) {
            out[i] = alpha * input[i] + (1 - alpha) * out[i - 1];
        }
        return out;
    }

    private double calculateVolumeRatio(double[] volumes, int idx) {
        if (idx < 5) {
            return 1.0;
        }
        double total = 0;
        for (int i = idx - 5; i < idx; i++) {
            total += volumes[i];
        }
        double avg = total / 5.0;
        return avg == 0 ? 1.0 : volumes[idx] / avg;
    }

    private record Macd(double[] dif, double[] dea, double[] hist) {
    }

    private record Kdj(double[] k, double[] d, double[] j) {
    }
}

