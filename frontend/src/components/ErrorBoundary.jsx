import React from 'react';

/**
 * ErrorBoundary — catches JavaScript errors anywhere in the child component
 * tree and displays a friendly fallback UI instead of a white blank screen.
 *
 * Usage: wrap your app (or a subtree) with <ErrorBoundary>
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    // In production you'd send this to Sentry / LogRocket / Datadog
    console.error('ErrorBoundary caught:', error, info);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#0a0f1c',
          fontFamily: "'Inter', sans-serif",
          padding: '2rem',
        }}>
          <div style={{
            background: 'rgba(255,255,255,0.04)',
            border: '1px solid rgba(255,255,255,0.08)',
            borderRadius: '16px',
            padding: '3rem',
            maxWidth: '480px',
            width: '100%',
            textAlign: 'center',
          }}>
            {/* Icon */}
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              background: 'rgba(239,68,68,0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem',
            }}>
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ef4444" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>

            <h1 style={{ color: '#fff', fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.75rem' }}>
              Something went wrong
            </h1>
            <p style={{ color: '#8b8fa3', fontSize: '0.9375rem', lineHeight: 1.6, marginBottom: '0.5rem' }}>
              An unexpected error occurred. Don't worry — your data is safe.
            </p>

            {/* Show error message in development */}
            {import.meta.env.DEV && this.state.error && (
              <pre style={{
                background: 'rgba(0,0,0,0.4)',
                border: '1px solid rgba(239,68,68,0.2)',
                borderRadius: '8px',
                padding: '1rem',
                marginTop: '1rem',
                marginBottom: '1.5rem',
                color: '#ef4444',
                fontSize: '0.75rem',
                textAlign: 'left',
                overflow: 'auto',
                maxHeight: '150px',
              }}>
                {this.state.error.toString()}
              </pre>
            )}

            <button
              onClick={this.handleReset}
              style={{
                marginTop: '1.5rem',
                background: 'linear-gradient(135deg, #10d9a0, #0ea5e9)',
                color: '#fff',
                border: 'none',
                borderRadius: '8px',
                padding: '0.75rem 2rem',
                fontSize: '0.9375rem',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'opacity 0.2s',
              }}
              onMouseOver={(e) => (e.target.style.opacity = '0.85')}
              onMouseOut={(e) => (e.target.style.opacity = '1')}
            >
              Return to Home
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
