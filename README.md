# Stock Analysis (Spring Boot + Vue)

A daily stock analysis app with:

- K-line (candlestick)
- KDJ
- MACD
- Volume
- Volume ratio
- Turnover
- LLM buy/sell/hold advice
- Web configuration UI

## Project Structure

- `backend/`: Java 17 + Spring Boot 3 REST API
- `frontend/`: Vue 3 + Vite + ECharts dashboard

## Features

- Configure watchlist symbols and LLM options in web UI
- Analyze single symbol or entire watchlist
- Daily scheduled analysis job (`18:00` by default)
- Rule-based fallback advice when LLM key is not configured

## Backend API

- `GET /api/health`
- `GET /api/config`
- `POST /api/config`
- `POST /api/analyze`
- `POST /api/analyze/watchlist?days=120`
- `GET /api/analysis/latest`

## Quick Start

### 1) Start backend

```bash
cd /Users/u0045596/IdeaProjects/stock_analysis/backend
mvn spring-boot:run
```

### 2) Start frontend

```bash
cd /Users/u0045596/IdeaProjects/stock_analysis/frontend
npm install
npm run dev
```

Then open `http://localhost:5173`.

If you do not want to rely on the Vite proxy, create `frontend/.env` from `frontend/.env.example`:

```bash
cp /Users/u0045596/IdeaProjects/stock_analysis/frontend/.env.example /Users/u0045596/IdeaProjects/stock_analysis/frontend/.env
```

## LLM Configuration

You can set these in environment variables for backend:

- `OPENAI_API_KEY`
- `OPENAI_BASE_URL` (default: `https://api.openai.com`)
- `LLM_MODEL` (default: `gpt-4.1-mini`)

Or configure in UI at runtime.

## Notes

- Market data source uses Yahoo Finance chart API for MVP.
- If the market data source is blocked by network policy, the frontend will now show a clear backend error instead of silently failing.
- This software provides research assistance only, not investment advice.

