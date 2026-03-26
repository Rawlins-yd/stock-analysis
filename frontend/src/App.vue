<template>
  <main>
    <h1>Stock Analysis Dashboard</h1>
    <p>Daily K-line, KDJ, MACD, volume, volume ratio, turnover, and LLM advice.</p>

    <div class="grid" style="margin-top: 14px">
      <section class="card">
        <h2 class="section-title">Configuration</h2>
        <label>
          Symbols (comma separated)
          <textarea v-model="symbolsInput" rows="3"></textarea>
        </label>
        <label>
          LLM Model
          <input v-model="config.llmModel" />
        </label>
        <label>
          LLM Base URL
          <input v-model="config.llmBaseUrl" />
        </label>
        <label>
          API Key
          <input v-model="config.llmApiKey" type="password" placeholder="sk-..." />
        </label>
        <button @click="save" :disabled="saving">{{ saving ? "Saving..." : "Save Config" }}</button>
        <div v-if="message" class="msg" :class="messageType">{{ message }}</div>

        <h3 style="margin-top: 18px">Run Analysis</h3>
        <label>
          Symbol
          <select v-model="selectedSymbol">
            <option v-for="s in config.symbols" :key="s" :value="s">{{ s }}</option>
          </select>
        </label>
        <label>
          Days
          <input v-model.number="days" type="number" min="40" max="300" />
        </label>
        <div class="row">
          <button @click="runOne" :disabled="loading">{{ loading ? "Analyzing..." : "Analyze Selected" }}</button>
          <button class="secondary" @click="runAll" :disabled="loading">Analyze Watchlist</button>
        </div>
      </section>

      <section class="card">
        <h2 class="section-title">Result</h2>
        <div v-if="result">
          <div class="kpi-grid">
            <div class="kpi"><strong>Symbol:</strong><br />{{ result.symbol }}</div>
            <div class="kpi"><strong>Close:</strong><br />{{ fmt(result.indicators.close) }}</div>
            <div class="kpi"><strong>Change%:</strong><br />{{ fmt(result.indicators.priceChangePct) }}</div>
            <div class="kpi"><strong>Volume Ratio:</strong><br />{{ fmt(result.indicators.volumeRatio) }}</div>
            <div class="kpi"><strong>Volume:</strong><br />{{ fmt(result.indicators.volume) }}</div>
            <div class="kpi"><strong>Turnover:</strong><br />{{ fmt(result.indicators.turnover) }}</div>
            <div class="kpi"><strong>MACD Signal:</strong><br />{{ result.indicators.macdSignal }}</div>
            <div class="kpi"><strong>KDJ Signal:</strong><br />{{ result.indicators.kdjSignal }}</div>
          </div>

          <h3 style="margin-top: 14px">Advice</h3>
          <div class="kpi-grid" style="grid-template-columns: repeat(3, minmax(120px, 1fr))">
            <div class="kpi"><strong>Action:</strong><br />{{ result.advice.action }}</div>
            <div class="kpi"><strong>Confidence:</strong><br />{{ result.advice.confidence }}%</div>
            <div class="kpi"><strong>Provider:</strong><br />{{ result.advice.provider }}</div>
          </div>
          <div class="list">
            <strong>Reasons</strong>
            <ul>
              <li v-for="(r, idx) in result.advice.reasons" :key="idx">{{ r }}</li>
            </ul>
            <p><strong>Risk:</strong> {{ result.advice.riskNote }}</p>
          </div>

          <StockCharts :candles="result.candles" />
        </div>
        <div v-else>No analysis result yet.</div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import StockCharts from "./components/StockCharts.vue";
import { analyze, analyzeWatchlist, getConfig, saveConfig } from "./api";

const config = reactive({
  symbols: ["AAPL", "MSFT"],
  llmModel: "gpt-4.1-mini",
  llmBaseUrl: "https://api.openai.com",
  llmApiKey: "",
});

const symbolsInput = ref("AAPL,MSFT");
const selectedSymbol = ref("AAPL");
const days = ref(120);
const loading = ref(false);
const saving = ref(false);
const message = ref("");
const messageType = ref("ok");
const result = ref(null);

function fmt(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return "--";
  }
  return Number(value).toLocaleString(undefined, { maximumFractionDigits: 4 });
}

function showError(err, fallbackMessage) {
  message.value = err?.response?.data?.error || err?.message || fallbackMessage;
  messageType.value = "error";
}

async function loadConfig() {
  try {
    const { data } = await getConfig();
    Object.assign(config, data);
    symbolsInput.value = (data.symbols || []).join(",");
    selectedSymbol.value = data.symbols?.[0] || "";
  } catch (err) {
    showError(err, "Failed to load configuration from backend");
  }
}

async function save() {
  saving.value = true;
  message.value = "";
  try {
    const symbols = symbolsInput.value
      .split(",")
      .map((x) => x.trim().toUpperCase())
      .filter(Boolean);

    const payload = {
      symbols,
      llmModel: config.llmModel,
      llmBaseUrl: config.llmBaseUrl,
      llmApiKey: config.llmApiKey,
    };

    const { data } = await saveConfig(payload);
    Object.assign(config, data);
    symbolsInput.value = (data.symbols || []).join(",");
    selectedSymbol.value = data.symbols?.[0] || "";
    message.value = "Configuration saved";
    messageType.value = "ok";
  } catch (err) {
    showError(err, "Save failed");
  } finally {
    saving.value = false;
  }
}

async function runOne() {
  if (!selectedSymbol.value) {
    message.value = "Please configure at least one stock symbol";
    messageType.value = "error";
    return;
  }
  loading.value = true;
  message.value = "";
  try {
    const { data } = await analyze(selectedSymbol.value, days.value);
    result.value = data;
    message.value = `Analysis finished for ${data.symbol}`;
    messageType.value = "ok";
  } catch (err) {
    showError(err, "Analyze request failed");
  } finally {
    loading.value = false;
  }
}

async function runAll() {
  loading.value = true;
  message.value = "";
  try {
    const { data } = await analyzeWatchlist(days.value);
    if (Array.isArray(data) && data.length > 0) {
      result.value = data[0];
      message.value = `Watchlist analysis finished (${data.length} symbols)`;
      messageType.value = "ok";
    } else {
      message.value = "No analysis result returned";
      messageType.value = "error";
    }
  } catch (err) {
    showError(err, "Watchlist analysis failed");
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadConfig();
});
</script>

