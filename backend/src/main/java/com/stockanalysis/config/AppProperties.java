package com.stockanalysis.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Llm llm = new Llm();
    private final Watchlist watchlist = new Watchlist();

    public Llm getLlm() {
        return llm;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public static class Llm {
        private String model = "gpt-4.1-mini";
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com";

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Watchlist {
        private List<String> symbols = new ArrayList<>(List.of("AAPL", "MSFT"));

        public List<String> getSymbols() {
            return symbols;
        }

        public void setSymbols(List<String> symbols) {
            this.symbols = symbols;
        }
    }
}

