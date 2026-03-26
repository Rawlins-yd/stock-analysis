package com.stockanalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockanalysis.model.Candle;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class YahooMarketDataService implements MarketDataService {

    private final RestTemplate restTemplate;

    public YahooMarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Candle> getDailyCandles(String symbol, int days) {
        String normalized = symbol.trim().toUpperCase();
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + normalized
                + "?interval=1d&range=" + Math.max(days, 1) + "d";

        try {
            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
            if (root == null) {
                throw new IllegalArgumentException("行情服务未返回数据: " + normalized);
            }

            JsonNode chartError = root.path("chart").path("error");
            if (!chartError.isMissingNode() && !chartError.isNull() && !chartError.isEmpty()) {
                String message = chartError.path("description").asText("");
                throw new IllegalArgumentException(message.isBlank()
                        ? "无法加载股票代码: " + normalized
                        : "无法加载股票代码 " + normalized + ": " + message);
            }

            JsonNode result = root.path("chart").path("result").get(0);
            if (result == null) {
                throw new IllegalArgumentException("没有查询到股票行情: " + normalized);
            }

            JsonNode timestamps = result.path("timestamp");
            JsonNode quote = result.path("indicators").path("quote").get(0);
            if (timestamps == null || quote == null) {
                throw new IllegalArgumentException("行情数据结构异常: " + normalized);
            }

            List<Candle> candles = new ArrayList<>();
            JsonNode opens = quote.path("open");
            JsonNode highs = quote.path("high");
            JsonNode lows = quote.path("low");
            JsonNode closes = quote.path("close");
            JsonNode volumes = quote.path("volume");

            int size = timestamps.size();
            for (int i = 0; i < size; i++) {
                if (isNullOrMissing(opens, i) || isNullOrMissing(highs, i) || isNullOrMissing(lows, i)
                        || isNullOrMissing(closes, i) || isNullOrMissing(volumes, i)) {
                    continue;
                }
                LocalDate date = Instant.ofEpochSecond(timestamps.get(i).asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                double close = closes.get(i).asDouble();
                double volume = volumes.get(i).asDouble();
                candles.add(new Candle(
                        date,
                        opens.get(i).asDouble(),
                        highs.get(i).asDouble(),
                        lows.get(i).asDouble(),
                        close,
                        volume,
                        close * volume
                ));
            }

            if (candles.size() < 35) {
                throw new IllegalArgumentException("可用行情数据不足，无法计算指标: " + normalized);
            }
            return candles;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("获取股票行情失败，请检查网络或数据源是否可访问: " + normalized);
        }
    }

    private boolean isNullOrMissing(JsonNode node, int index) {
        return node.get(index) == null || node.get(index).isNull();
    }
}

