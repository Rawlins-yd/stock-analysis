<template>
  <div>
    <div ref="klineRef" class="chart"></div>
    <div ref="macdRef" class="chart"></div>
    <div ref="kdjRef" class="chart"></div>
    <div ref="volumeRef" class="chart"></div>
  </div>
</template>

<script setup>
import * as echarts from "echarts";
import { onBeforeUnmount, onMounted, ref, watch } from "vue";

const props = defineProps({
  candles: {
    type: Array,
    default: () => [],
  },
});

const klineRef = ref(null);
const macdRef = ref(null);
const kdjRef = ref(null);
const volumeRef = ref(null);

let klineChart;
let macdChart;
let kdjChart;
let volumeChart;

function resizeCharts() {
  klineChart?.resize();
  macdChart?.resize();
  kdjChart?.resize();
  volumeChart?.resize();
}

function ema(values, period) {
  const alpha = 2 / (period + 1);
  const result = [values[0]];
  for (let i = 1; i < values.length; i += 1) {
    result.push(alpha * values[i] + (1 - alpha) * result[i - 1]);
  }
  return result;
}

function calcMacd(closes) {
  const ema12 = ema(closes, 12);
  const ema26 = ema(closes, 26);
  const dif = closes.map((_, i) => ema12[i] - ema26[i]);
  const dea = ema(dif, 9);
  const hist = dif.map((v, i) => (v - dea[i]) * 2);
  return { dif, dea, hist };
}

function calcKdj(candles) {
  const k = [50];
  const d = [50];
  const j = [50];
  for (let i = 1; i < candles.length; i += 1) {
    const start = Math.max(0, i - 8);
    let high = candles[start].high;
    let low = candles[start].low;
    for (let idx = start + 1; idx <= i; idx += 1) {
      high = Math.max(high, candles[idx].high);
      low = Math.min(low, candles[idx].low);
    }
    const rsv = high === low ? 50 : ((candles[i].close - low) / (high - low)) * 100;
    k.push((2 * k[i - 1] + rsv) / 3);
    d.push((2 * d[i - 1] + k[i]) / 3);
    j.push(3 * k[i] - 2 * d[i]);
  }
  return { k, d, j };
}

function renderCharts() {
  if (!klineChart || !macdChart || !kdjChart || !volumeChart) {
    return;
  }

  if (!Array.isArray(props.candles) || !props.candles.length) {
    klineChart.clear();
    macdChart.clear();
    kdjChart.clear();
    volumeChart.clear();
    return;
  }

  const validCandles = props.candles.filter(
    (c) => c && [c.date, c.open, c.close, c.low, c.high, c.volume].every((value) => value !== null && value !== undefined)
  );

  if (!validCandles.length) {
    klineChart.clear();
    macdChart.clear();
    kdjChart.clear();
    volumeChart.clear();
    return;
  }

  const dates = validCandles.map((c) => c.date);
  const closes = validCandles.map((c) => c.close);
  const ohlc = validCandles.map((c) => [c.open, c.close, c.low, c.high]);
  const volumes = validCandles.map((c) => c.volume);
  const { dif, dea, hist } = calcMacd(closes);
  const { k, d, j } = calcKdj(validCandles);

  klineChart.setOption({
    title: { text: "K-line" },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: dates },
    yAxis: { scale: true },
    series: [{ type: "candlestick", data: ohlc }],
  });

  macdChart.setOption({
    title: { text: "MACD" },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: dates },
    yAxis: [{ type: "value" }, { type: "value" }],
    series: [
      { name: "DIF", type: "line", data: dif, yAxisIndex: 0, smooth: true },
      { name: "DEA", type: "line", data: dea, yAxisIndex: 0, smooth: true },
      { name: "HIST", type: "bar", data: hist, yAxisIndex: 1 },
    ],
  });

  kdjChart.setOption({
    title: { text: "KDJ" },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: dates },
    yAxis: { type: "value" },
    series: [
      { name: "K", type: "line", data: k, smooth: true },
      { name: "D", type: "line", data: d, smooth: true },
      { name: "J", type: "line", data: j, smooth: true },
    ],
  });

  volumeChart.setOption({
    title: { text: "Volume" },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: dates },
    yAxis: { type: "value" },
    series: [{ type: "bar", data: volumes }],
  });
}

onMounted(() => {
  klineChart = echarts.init(klineRef.value);
  macdChart = echarts.init(macdRef.value);
  kdjChart = echarts.init(kdjRef.value);
  volumeChart = echarts.init(volumeRef.value);
  window.addEventListener("resize", resizeCharts);
  renderCharts();
});

watch(
  () => props.candles,
  () => {
    renderCharts();
  },
  { deep: true }
);

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeCharts);
  klineChart?.dispose();
  macdChart?.dispose();
  kdjChart?.dispose();
  volumeChart?.dispose();
});
</script>

