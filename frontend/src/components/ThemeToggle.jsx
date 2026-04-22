import React from 'react';
import { useTheme } from '../hooks/useTheme';

const ThemeToggle = () => {
  const { theme, toggleTheme } = useTheme();
  const isDark = theme === 'dark';

  return (
    <button
      onClick={toggleTheme}
      title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
      aria-label="Toggle theme"
      style={{
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        width: '52px',
        height: '28px',
        borderRadius: '100px',
        border: '1px solid var(--border-hover)',
        background: isDark
          ? 'linear-gradient(135deg, #1e1b4b 0%, #0f172a 100%)'
          : 'linear-gradient(135deg, #fef9c3 0%, #fde68a 100%)',
        cursor: 'pointer',
        padding: '3px',
        transition: 'background 0.4s ease, border-color 0.4s ease',
        outline: 'none',
        flexShrink: 0,
        boxShadow: isDark
          ? '0 0 10px rgba(79, 70, 229, 0.3), inset 0 1px 2px rgba(0,0,0,0.4)'
          : '0 0 10px rgba(251, 191, 36, 0.4), inset 0 1px 2px rgba(0,0,0,0.1)',
      }}
    >
      {/* Sliding knob */}
      <span
        style={{
          position: 'absolute',
          top: '3px',
          left: isDark ? '27px' : '3px',
          width: '20px',
          height: '20px',
          borderRadius: '50%',
          background: isDark
            ? 'linear-gradient(135deg, #c7d2fe 0%, #818cf8 100%)'
            : 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)',
          transition: 'left 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275), background 0.4s ease',
          boxShadow: isDark
            ? '0 2px 6px rgba(0,0,0,0.5)'
            : '0 2px 6px rgba(245,158,11,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '11px',
        }}
      >
        {isDark ? '🌙' : '☀️'}
      </span>
    </button>
  );
};

export default ThemeToggle;
