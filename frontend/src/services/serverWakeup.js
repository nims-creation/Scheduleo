/**
 * serverWakeup.js
 *
 * Solves the Render.com free-tier cold-start problem.
 *
 * Strategy:
 *  1. On app boot → immediately ping /actuator/health (or /api/v1/ping).
 *     This fires BEFORE the user clicks anything, so by the time they
 *     hit "Login" the server is already warm.
 *  2. Keep-alive interval → ping every 13 minutes so the server never
 *     sleeps during an active browser session.
 *  3. Expose a status observable so the UI can show a banner if the
 *     server is still cold (response takes > 4 s).
 */

import { API_URL } from './api';

// Public health / ping endpoint — no auth required
const PING_URL = `${API_URL}/api/v1/auth/ping`;

// How long (ms) before we consider the server "cold" and show a banner
const COLD_THRESHOLD_MS = 3500;

// Keep-alive interval: 13 min (Render sleeps after 15 min inactivity)
const KEEPALIVE_INTERVAL_MS = 13 * 60 * 1000;

let keepAliveTimer = null;
const listeners = new Set();

/** Internal: notify all status listeners */
function emit(status) {
  listeners.forEach((fn) => fn(status));
}

/**
 * Subscribe to server status changes.
 * status = 'checking' | 'warm' | 'cold' | 'waking' | 'online'
 * Returns an unsubscribe function.
 */
export function onServerStatus(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

/** Fire one ping and return true if warm (fast), false if cold (slow) */
async function ping() {
  const start = Date.now();
  try {
    const controller = new AbortController();
    // Give up after 90 s — Render cold start can take ~60-80 s
    const timeout = setTimeout(() => controller.abort(), 90_000);

    const res = await fetch(PING_URL, {
      method: 'GET',
      signal: controller.signal,
      cache: 'no-store',
    });
    clearTimeout(timeout);

    const elapsed = Date.now() - start;
    return { ok: res.ok || res.status < 500, elapsed };
  } catch {
    return { ok: false, elapsed: Date.now() - start };
  }
}

/**
 * Call this once at app startup (from main.jsx or App.jsx).
 * It fires an instant ping and schedules keep-alive pings.
 */
export async function initServerWakeup() {
  emit('checking');

  const { ok, elapsed } = await ping();

  if (!ok) {
    // Server was sleeping — it woke up mid-ping (Render boots on first request)
    emit('waking');
    // Wait for a second ping to confirm it's fully up
    const second = await ping();
    emit(second.ok ? 'online' : 'waking');
  } else if (elapsed > COLD_THRESHOLD_MS) {
    emit('waking');
  } else {
    emit('warm');
  }

  // Schedule keep-alive so server doesn't sleep while user is browsing
  if (keepAliveTimer) clearInterval(keepAliveTimer);
  keepAliveTimer = setInterval(async () => {
    await ping();
  }, KEEPALIVE_INTERVAL_MS);
}

/** Call on app unmount / logout if you want to stop keep-alive */
export function stopServerWakeup() {
  if (keepAliveTimer) {
    clearInterval(keepAliveTimer);
    keepAliveTimer = null;
  }
}
