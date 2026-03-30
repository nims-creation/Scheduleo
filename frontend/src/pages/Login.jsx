import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Calendar, Mail, Lock, Loader2 } from 'lucide-react';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showForgotModal, setShowForgotModal] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotLoading, setForgotLoading] = useState(false);
  const [toast, setToast] = useState(null);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    if(!forgotEmail) return;
    setForgotLoading(true);
    try {
      // Need to import api if it wasn't here, but wait, api is not imported in Login.jsx?
      // Wait, is api imported? Let's check imports. No, it's not. I will add import dynamically.
      const api = (await import('../services/api')).default;
      await api.post(`/api/v1/auth/forgot-password?email=${encodeURIComponent(forgotEmail)}`);
      setToast({ msg: 'If the email exists, a reset link has been sent.', type: 'success' });
      setShowForgotModal(false);
      setForgotEmail('');
    } catch (err) {
      setToast({ msg: 'Failed to initiate password reset.', type: 'error' });
    } finally {
      setForgotLoading(false);
      setTimeout(() => setToast(null), 3000);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    
    const result = await login(email, password);
    
    setIsLoading(false);
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
  };

  return (
    <div className="flex-center page-wrapper animate-fade-in" style={{ backgroundColor: 'var(--bg-primary)' }}>
      {/* Absolute Header */}
      <div style={{ position: 'absolute', top: '2rem', left: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none', color: 'inherit' }}>
          <div style={{ background: 'var(--brand-gradient)', padding: '0.5rem', borderRadius: 'var(--radius-sm)' }}>
            <Calendar color="white" size={20} />
          </div>
          <span style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>Schedulo</span>
        </Link>
      </div>

      <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '420px', padding: '2.5rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.75rem' }}>Welcome Back</h2>
          <p style={{ color: 'var(--text-secondary)' }}>Sign in to continue to your dashboard</p>
        </div>

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="input-group">
            <label className="input-label">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
              <input 
                type="email" 
                required 
                className="input-field" 
                style={{ paddingLeft: '2.5rem' }}
                placeholder="you@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div className="input-group">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="input-label">Password</label>
              <a href="#" onClick={(e) => { e.preventDefault(); setShowForgotModal(true); }} style={{ fontSize: '0.75rem', color: 'var(--brand-primary)', textDecoration: 'none' }}>Forgot password?</a>
            </div>
            <div style={{ position: 'relative' }}>
              <Lock style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
              <input 
                type="password" 
                required 
                className="input-field" 
                style={{ paddingLeft: '2.5rem' }}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%', marginTop: '1rem' }}
            disabled={isLoading}
          >
            {isLoading ? <Loader2 className="animate-spin" size={20} /> : 'Sign In'}
          </button>
        </form>

        <div style={{ display: 'flex', alignItems: 'center', margin: '1.5rem 0', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.1)' }}></div>
          <span style={{ margin: '0 1rem' }}>OR</span>
          <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.1)' }}></div>
        </div>

        <button 
          type="button" 
          onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/google'}
          className="btn btn-secondary" 
          style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', background: '#ffffff', color: '#1a1a1a' }}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
          </svg>
          Continue with Google
        </button>

        <div style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          Don't have an account? <Link to="/signup" style={{ color: 'var(--brand-primary)', textDecoration: 'none', fontWeight: '500' }}>Sign up</Link>
        </div>
      </div>

      {showForgotModal && (
        <div className="modal-overlay">
          <div className="modal-box" style={{ padding: '2.5rem', position: 'relative' }}>
            <button onClick={() => setShowForgotModal(false)} className="modal-close" style={{ position: 'absolute', top: '1rem', right: '1rem' }}>✕</button>
            <h3 className="modal-title" style={{ marginBottom: '0.5rem' }}>Reset Password</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>Enter your email address and we'll send you a link to reset your password.</p>
            <form onSubmit={handleForgotPassword}>
              <div className="input-group">
                <input 
                  type="email" 
                  required 
                  className="input-field" 
                  placeholder="you@company.com"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                />
              </div>
              <button 
                type="submit" 
                className="btn btn-primary" 
                style={{ width: '100%', marginTop: '0.5rem' }}
                disabled={forgotLoading}
              >
                {forgotLoading ? <Loader2 className="animate-spin" size={20} /> : 'Send Reset Link'}
              </button>
            </form>
          </div>
        </div>
      )}

      {toast && (
        <div className="toast-container">
          <div className={`toast toast-${toast.type}`}>{toast.msg}</div>
        </div>
      )}
    </div>
  );
};

export default Login;
