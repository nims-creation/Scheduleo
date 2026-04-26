import React, { useEffect, useState } from 'react';
import { onServerStatus } from '../services/serverWakeup';

/**
 * WakeupBanner
 *
 * Shows a non-intrusive banner at the TOP of the screen when the
 * backend is cold-starting. Disappears automatically once warm.
 * Does NOT block any UI — users can still browse the landing page.
 */

const MESSAGES = {
  checking: { text: 'Connecting to server…', color: '#06b6d4', pulse: true },
  waking:   { text: '⏳ Server is waking up — this takes ~30s on first visit. Please wait…', color: '#f59e0b', pulse: true },
  warm:     { text: null },   // already fast — don't show anything
  online:   { text: '✅ Server is ready!', color: '#10d9a0', pulse: false, autoDismiss: 2500 },
};

export default function WakeupBanner() {
  const [status, setStatus] = useState(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const unsub = onServerStatus((s) => {
      const msg = MESSAGES[s];
      if (!msg || msg.text === null) {
        // warm → never show
        setVisible(false);
        return;
      }
      setStatus(s);
      setVisible(true);

      if (msg.autoDismiss) {
        setTimeout(() => setVisible(false), msg.autoDismiss);
      }
    });
    return unsub;
  }, []);

  if (!visible || !status) return null;

  const msg = MESSAGES[status];

  return (
    <div
      role="status"
      aria-live="polite"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 9999,
        padding: '0.6rem 1.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '0.65rem',
        background: status === 'waking'
          ? 'rgba(245,158,11,0.12)'
          : 'rgba(6,182,212,0.1)',
        backdropFilter: 'blur(12px)',
        borderBottom: `1px solid ${msg.color}30`,
        fontSize: '0.82rem',
        fontWeight: 600,
        fontFamily: "'Outfit', sans-serif",
        color: msg.color,
        transition: 'all 0.4s ease',
      }}
    >
      {/* Animated dot */}
      {msg.pulse && (
        <span style={{
          width: 7,
          height: 7,
          borderRadius: '50%',
          background: msg.color,
          display: 'inline-block',
          flexShrink: 0,
          animation: 'wakeupPulse 1.4s ease-out infinite',
        }} />
      )}
      {msg.text}

      <style>{`
        @keyframes wakeupPulse {
          0%   { box-shadow: 0 0 0 0 ${msg.color}80; }
          70%  { box-shadow: 0 0 0 7px ${msg.color}00; }
          100% { box-shadow: 0 0 0 0 ${msg.color}00; }
        }
      `}</style>
    </div>
  );
}
