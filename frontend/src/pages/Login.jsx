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
