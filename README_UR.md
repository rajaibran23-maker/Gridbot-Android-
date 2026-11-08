# MetaApi MT5 Bridge (server-side)

Yeh service MetaApi ka secret token server par rakhti hai. Token ko APK mein kabhi hard-code na karein.
MetaApi docs: account create/deploy, positions/account info aur trade REST endpoints.

## Run
1. Node.js 20+ server/VPS/cloud host par install karein.
2. `cp .env.example .env` karein aur apna MetaApi token, regional API URLs aur long random `BRIDGE_API_KEY` set karein.
3. `npm install`
4. `npm start`
5. HTTPS reverse proxy lagayen. Public internet par HTTP expose na karein.

## API
All `/api/*` requests need `x-bridge-key` header.

- `POST /api/accounts` body `{ "login":"...", "password":"...", "server":"BROKER-DEMO-SERVER", "platform":"mt5" }`
- `POST /api/accounts/:id/deploy`
- `GET /api/accounts/:id/status`
- `GET /api/accounts/:id/account-information`
- `GET /api/accounts/:id/positions`
- `POST /api/accounts/:id/trades` body `{ "side":"BUY", "symbol":"XAUUSD", "volume":0.01 }`
- `POST /api/accounts/:id/positions/:positionId/close`

## Safety
- First use a DEMO account. MetaApi cloud account provisioning may be billable.
- Never send credentials or API tokens in ChatGPT. Configure secrets directly on your server.
- The bridge is an execution adapter, NOT the finished strategy runner. The app must still poll quotes/positions, calculate commission-inclusive basket P/L, persist basket state, prevent duplicate orders and execute close-all/ emergency-stop reliably before Start can be enabled.
- Protect this API with HTTPS, strong private key, rate limits, per-user authorization and audit logs before production.
- Confirm your MetaApi account's regional API host from its dashboard. The default client URL in `.env.example` is New York only.
