import { useState, useEffect } from 'react';
import { API_URL } from '../services/api';

/**
 * Silently wakes the Render free-tier backend when auth pages mount.
 *
 * Render spins down after 15 min of inactivity. When a user hits the login
 * page the server may be cold — this hook fires a health-check immediately so
 * the JVM boots *while* the user types their credentials, not after they submit.
 *
 * Returns { isWakingUp } — true only during the first few seconds so the UI
 * can optionally show a "Server is starting…" notice.
 */
export function useServerWakeup() {
  const [isWakingUp, setIsWakingUp] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const SLOW_THRESHOLD_MS = 2000; // only show banner if cold-start takes > 2s

    const start = Date.now();
    const timer = setTimeout(() => {
      // If no response yet after threshold → server is probably cold
      if (!cancelled) setIsWakingUp(true);
    }, SLOW_THRESHOLD_MS);

    fetch(`${API_URL}/actuator/health`, {
      method: 'GET',
      // Don't send auth headers — this endpoint is public
      cache: 'no-store',
    })
      .catch(() => {/* ignore errors — this is best-effort */})
      .finally(() => {
        if (!cancelled) {
          clearTimeout(timer);
          setIsWakingUp(false);
        }
      });

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, []); // run once on mount

  return { isWakingUp };
}
