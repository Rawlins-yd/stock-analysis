package com.stockanalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockanalysis.model.AdviceResult;
import com.stockanalysis.model.IndicatorSnapshot;
import com.stockanalysis.model.RuntimeConfig;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LlmAdvisoryService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LlmAdvisoryService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AdviceResult advise(String symbol, IndicatorSnapshot indicators, RuntimeConfig config) {
        if (config.getLlmApiKey() == null || config.getLlmApiKey().isBlank()) {
            return fallbackRuleBased(indicators);
        }

        try {
            String prompt = buildPrompt(symbol, indicators);
            String url = config.getLlmBaseUrl().replaceAll("/$", "") + "/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.getLlmApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = Map.of(
                    "model", config.getLlmModel(),
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是谨慎的股票分析助手。仅基于给定指标给出建议，输出必须是JSON。"),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "response_format", Map.of("type", "json_object")
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);
            JsonNode contentNode = response.getBody()
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content");

            JsonNode json = objectMapper.readTree(contentNode.asText("{}"));
            String action = json.path("action").asText("hold");
            int confidence = json.path("confidence").asInt(55);
            List<String> reasons = json.path("reasons").isArray()
                    ? objectMapper.convertValue(json.path("reasons"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class))
                    : List.of("LLM未返回有效理由");
            String riskNote = json.path("riskNote").asText("市场波动较大，请控制仓位。");

            return new AdviceResult(action, Math.max(0, Math.min(confidence, 100)), reasons, riskNote, "llm");
        } catch (Exception ex) {
            return fallbackRuleBased(indicators);
        }
    }

    private String buildPrompt(String symbol, IndicatorSnapshot s) {
        return "股票: " + symbol + "\n"
                + "收盘价: " + s.close() + "\n"
                + "涨跌幅(%): " + s.priceChangePct() + "\n"
                + "MACD(DIF/DEA/HIST): " + s.macdDif() + "/" + s.macdDea() + "/" + s.macdHist() + "\n"
                + "MACD信号: " + s.macdSignal() + "\n"
                + "KDJ(K/D/J): " + s.k() + "/" + s.d() + "/" + s.j() + "\n"
                + "KDJ信号: " + s.kdjSignal() + "\n"
                + "成交量: " + s.volume() + "\n"
                + "量比: " + s.volumeRatio() + "\n"
                + "成交额: " + s.turnover() + "\n\n"
                + "请输出JSON，字段: action(buy/sell/hold), confidence(0-100), reasons(字符串数组), riskNote。";
    }

    private AdviceResult fallbackRuleBased(IndicatorSnapshot s) {
        int score = 0;
        if ("golden".equals(s.macdSignal())) {
            score += 2;
        }
        if ("dead".equals(s.macdSignal())) {
            score -= 2;
        }
        if ("golden".equals(s.kdjSignal())) {
            score += 1;
        }
        if ("dead".equals(s.kdjSignal())) {
            score -= 1;
        }
        if (s.volumeRatio() > 1.3 && s.priceChangePct() > 0) {
            score += 1;
        }
        if (s.volumeRatio() > 1.8 && s.priceChangePct() < 0) {
            score -= 1;
        }

        String action = score >= 2 ? "buy" : (score <= -2 ? "sell" : "hold");
        int confidence = 55 + Math.abs(score) * 10;
        return new AdviceResult(
                action,
                Math.min(confidence, 85),
                List.of("基于MACD、KDJ和量价关系的规则引擎结果", "未配置可用LLM或LLM调用失败，已降级"),
                "仅供参考，不构成投资建议。",
                "rules"
        );
    }
}

