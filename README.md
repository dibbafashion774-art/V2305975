# BinEx — Professional Cryptocurrency Exchange

BinEx is a high-performance, professional Android cryptocurrency exchange application built with modern Android architecture and Jetpack Compose. Inspired by the refined aesthetics and speed of Binance, BinEx features a dark theme with gold accents, live real-time Binance public market data (REST + WebSocket streams), interactive financial charts, an order book with depth visualizations, and an spot simulation trading engine with strict mathematical and portfolio balance integrity.

---

## 🌟 Key Features

1. **Dashboard & Portfolio Management**
   - **Initial Portfolio:** 7,000,000.00 USDT starting demo balance
   - **Real-Time Portfolio Valuation:** Automatically updates based on live quotes from Binance
   - **PnL Tracking:** Real-time unrealized and today's profit/loss calculations with percentages
   - **Balance Privacy:** Eye toggle to hide / reveal sensitive balances
   - **Quick Actions:** Instant shortcuts to Buy, Sell, Markets, Trade, Assets, and Transaction History

2. **Real-Time Market Data Integration**
   - **Binance Public REST API:** 24hr tickers, historical klines (candlesticks), order book depth, and recent market trades
   - **Binance WebSocket Stream (`!miniTicker@arr`):** Continuous, real-time ticker stream with auto-reconnection and connection health status indicator (LIVE, SYNC, OFFLINE)
   - **Supported Trading Pairs:** BTC/USDT, ETH/USDT, BNB/USDT, SOL/USDT, XRP/USDT, DOGE/USDT, ADA/USDT, TRX/USDT, AVAX/USDT, LINK/USDT, LTC/USDT, DOT/USDT, BCH/USDT, SUI/USDT, TON/USDT, and more
   - **Mini Sparklines:** Canvas-based price trendlines rendered across market rows and cards

3. **Markets & Discovery**
   - **Categorization:** All Coins, Favorites (with local persistence), Top Gainers, Top Losers, 24h Volume
   - **Search & Sort:** Instant query filter by symbol/name and multi-attribute sorting (Price, 24h Change, Volume, Name)

4. **Interactive Financial Charting**
   - **Multiple Intervals:** 1m, 5m, 15m, 1h, 4h, 1D, 1W
   - **Dual Display Modes:** Japanese Candlesticks with wicks and bodies, or glowing Line/Area chart with volume bars
   - **Touch Scrubber Tooltip:** Drag along the chart to inspect exact candle metrics (Timestamp, Open, High, Low, Close, Change %, Volume)

5. **Spot Trading Interface**
   - **Order Types:** Market Orders (instant execution at best price) and Limit Orders (placed in order book)
   - **Order Book Integration:** Visual bid/ask depth bars with one-tap price fill into the order input
   - **Quick Allocation:** 25%, 50%, 75%, 100% quick amount calculation chips
   - **Strict Validations:** Prevents invalid orders, negative balances, and over-allocation
   - **Order History & Open Orders:** Manage and cancel open limit orders

6. **Wallet & Assets**
   - **Multi-Wallet Support:** Spot Wallet and Funding Wallet
   - **Asset Allocation:** Visual distribution breakdown between cash USDT and cryptocurrency positions
   - **Internal Transfers:** Instant transfer between Spot and Funding wallets
   - **Deposit & Withdraw:** Simulation deposit & withdrawal flows with network selection (TRC20, BEP20, ERC20)

7. **Account, Security & Simulation Controls**
   - Biometric App Lock toggle
   - Two-Factor Authentication toggle
   - Reset Simulation Balance button (restores pristine 7,000,000.00 USDT starting portfolio)
   - Transparent terms of service and privacy policies

---

## 🏗️ Technical Architecture

- **UI Framework:** 100% Jetpack Compose with Material 3
- **Design System:** Custom Binance Dark Theme (`#181A20`, `#0B0E11`, `#1E2329`) with Binance Gold (`#F0B90B`, `#FCD535`), Crypto Green (`#0ECB81`), and Crypto Red (`#F6465D`)
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (StateFlow)
- **Local Persistence:** Room Database (`AppDatabase`) with SQLite for portfolio, holdings, orders, transactions, and favorites
- **Networking:** Retrofit 2 + Moshi + OkHttp WebSocket
- **Testing:** JUnit 4 + Robolectric unit & integration test suites

---

## 🧪 Testing

Run all unit and Robolectric tests using Gradle:

```bash
gradle :app:testDebugUnitTest
```

---

## 📦 Building the APK

### Debug APK
To assemble the debug APK:

```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Release APK
To assemble the release APK:

```bash
gradle :app:assembleRelease
```
The generated APK will be located at:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed if keystore configured).
