package com.stockanalysis.service;

import com.stockanalysis.config.AppProperties;
import com.stockanalysis.model.RuntimeConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    private final AtomicReference<RuntimeConfig> configRef;

    public ConfigService(AppProperties appProperties) {
        RuntimeConfig initial = new RuntimeConfig();
        initial.setSymbols(new ArrayList<>(appProperties.getWatchlist().getSymbols()));
        initial.setLlmModel(appProperties.getLlm().getModel());
        initial.setLlmBaseUrl(appProperties.getLlm().getBaseUrl());
        initial.setLlmApiKey(appProperties.getLlm().getApiKey());
        this.configRef = new AtomicReference<>(initial);
    }

    public RuntimeConfig getConfig() {
        return copy(configRef.get());
    }

    public RuntimeConfig update(RuntimeConfig incoming) {
        RuntimeConfig clean = sanitize(incoming);
        configRef.set(clean);
        return copy(clean);
    }

    public List<String> symbols() {
        return List.copyOf(configRef.get().getSymbols());
    }

    private RuntimeConfig sanitize(RuntimeConfig input) {
        RuntimeConfig clean = new RuntimeConfig();
        clean.setSymbols(input.getSymbols().stream()
                .map(symbol -> symbol.trim().toUpperCase())
                .filter(symbol -> !symbol.isBlank())
                .distinct()
                .toList());
        clean.setLlmModel(input.getLlmModel() == null || input.getLlmModel().isBlank()
                ? "gpt-4.1-mini"
                : input.getLlmModel().trim());
        clean.setLlmBaseUrl(input.getLlmBaseUrl() == null || input.getLlmBaseUrl().isBlank()
                ? "https://api.openai.com"
                : input.getLlmBaseUrl().trim());
        clean.setLlmApiKey(input.getLlmApiKey() == null ? "" : input.getLlmApiKey().trim());
        return clean;
    }

    private RuntimeConfig copy(RuntimeConfig src) {
        RuntimeConfig out = new RuntimeConfig();
        out.setSymbols(new ArrayList<>(src.getSymbols()));
        out.setLlmModel(src.getLlmModel());
        out.setLlmBaseUrl(src.getLlmBaseUrl());
        out.setLlmApiKey(src.getLlmApiKey());
        return out;
    }
}

