import express from 'express';
import helmet from 'helmet';
import crypto from 'node:crypto';

const app = express();
app.use(helmet());
app.use(express.json({ limit: '32kb' }));

const {
  METAAPI_TOKEN, METAAPI_PROVISIONING_URL, METAAPI_CLIENT_URL,
  BRIDGE_API_KEY, PORT = 8080
} = process.env;

if (!METAAPI_TOKEN || !METAAPI_PROVISIONING_URL || !METAAPI_CLIENT_URL || !BRIDGE_API_KEY) {
  throw new Error('Missing server environment variables. Copy .env.example to .env and configure it.');
}

function authorized(req, res, next) {
  const supplied = req.get('x-bridge-key') || '';
  const a = Buffer.from(supplied);
  const b = Buffer.from(BRIDGE_API_KEY);
  if (a.length !== b.length || !crypto.timingSafeEqual(a, b)) {
    return res.status(401).json({ error: 'Unauthorized bridge client' });
  }
  next();
}
app.use('/api', authorized);

async function meta(path, { base = METAAPI_CLIENT_URL, method = 'GET', body } = {}) {
  const response = await fetch(`${base}${path}`, {
    method,
    headers: {
      'auth-token': METAAPI_TOKEN,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    ...(body === undefined ? {} : { body: JSON.stringify(body) })
  });
  const raw = await response.text();
  let data;
  try { data = raw ? JSON.parse(raw) : {}; } catch { data = { raw }; }
  if (!response.ok) {
    const err = new Error(data.message || `MetaApi HTTP ${response.status}`);
    err.status = response.status;
    err.details = data;
    throw err;
  }
  return data;
}
function accountPath(id, suffix = '') {
  if (!/^[a-zA-Z0-9-]{8,80}$/.test(id)) throw Object.assign(new Error('Invalid MetaApi account id'), { status: 400 });
  return `/users/current/accounts/${id}${suffix}`;
}
function handleError(err, res) {
  res.status(err.status || 502).json({ error: err.message, details: err.details || null });
}

app.get('/health', (_req, res) => res.json({ ok: true, service: 'gridbot-metaapi-bridge' }));

// Provision the user's MT5 account. Use a DEMO account first.
// The MT5 password is forwarded only to MetaApi over TLS and is not logged by this service.
app.post('/api/accounts', async (req, res) => {
  try {
    const { login, password, server, name = 'GridBot MT5 Demo', platform = 'mt5', magic = 7261107 } = req.body || {};
    if (!login || !password || !server || platform !== 'mt5') {
      return res.status(400).json({ error: 'login, password, server and platform=mt5 are required' });
    }
    const idempotency = crypto.randomUUID().replaceAll('-', '');
    const data = await meta('/users/current/accounts', {
      base: METAAPI_PROVISIONING_URL, method: 'POST',
      body: { login: String(login), password: String(password), server: String(server),
        name: String(name), platform: 'mt5', magic: Number(magic), type: 'cloud-g2', reliability: 'high' }
    });
    res.status(201).json({ accountId: data.id, state: data.state, note: 'Account created. Deploy and wait for connection status before enabling trading.' });
  } catch (e) { handleError(e, res); }
});

app.post('/api/accounts/:id/deploy', async (req, res) => {
  try {
    await meta(accountPath(req.params.id, '/deploy'), { base: METAAPI_PROVISIONING_URL, method: 'POST' });
    res.json({ deployed: true });
  } catch (e) { handleError(e, res); }
});

app.get('/api/accounts/:id/status', async (req, res) => {
  try {
    const data = await meta(accountPath(req.params.id));
    res.json({ id: data.id, state: data.state, connectionStatus: data.connectionStatus || null,
      platform: data.platform, server: data.server, login: data.login });
  } catch (e) { handleError(e, res); }
});

app.get('/api/accounts/:id/account-information', async (req, res) => {
  try { res.json(await meta(accountPath(req.params.id, '/account-information'))); }
  catch (e) { handleError(e, res); }
});

app.get('/api/accounts/:id/positions', async (req, res) => {
  try { res.json(await meta(accountPath(req.params.id, '/positions'))); }
  catch (e) { handleError(e, res); }
});

app.post('/api/accounts/:id/trades', async (req, res) => {
  try {
    const { side, symbol, volume, stopLoss, takeProfit, clientId } = req.body || {};
    if (!['BUY', 'SELL'].includes(side) || !/^[A-Za-z0-9._-]{3,24}$/.test(symbol || '') ||
        ![0.01, 0.02, 0.03, 0.05].includes(Number(volume))) {
      return res.status(400).json({ error: 'Only BUY/SELL, valid symbol, and configured grid lot sizes 0.01/0.02/0.03/0.05 are accepted.' });
    }
    const trade = { actionType: side === 'BUY' ? 'ORDER_TYPE_BUY' : 'ORDER_TYPE_SELL',
      symbol, volume: Number(volume), magic: 7261107,
      ...(Number.isFinite(Number(stopLoss)) && Number(stopLoss) > 0 ? { stopLoss: Number(stopLoss) } : {}),
      ...(Number.isFinite(Number(takeProfit)) && Number(takeProfit) > 0 ? { takeProfit: Number(takeProfit) } : {}),
      ...(clientId ? { clientId: String(clientId).slice(0, 26) } : {}) };
    const result = await meta(accountPath(req.params.id, '/trade'), { method: 'POST', body: trade });
    res.json(result);
  } catch (e) { handleError(e, res); }
});

app.post('/api/accounts/:id/positions/:positionId/close', async (req, res) => {
  try {
    const result = await meta(accountPath(req.params.id, '/trade'), {
      method: 'POST', body: { actionType: 'POSITION_CLOSE_ID', positionId: String(req.params.positionId), magic: 7261107 }
    });
    res.json(result);
  } catch (e) { handleError(e, res); }
});

app.listen(Number(PORT), '0.0.0.0', () => {
  console.log(`GridBot bridge listening on ${PORT}`);
});
