import axios from "axios";

const configuredBaseUrl = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");

const http = axios.create({
  baseURL: configuredBaseUrl,
  timeout: 15000,
});

export const getConfig = () => http.get("/config");
export const saveConfig = (payload) => http.post("/config", payload);
export const analyze = (symbol, days = 120) => http.post("/analyze", { symbol, days });
export const analyzeWatchlist = (days = 120) => http.post(`/analyze/watchlist?days=${days}`);
export const getLatest = () => http.get("/analysis/latest");

