import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Loader2 } from 'lucide-react';

const OAuth2RedirectHandler = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { loginWithToken } = useAuth();
  const [errorMsg, setErrorMsg] = useState(null);

  useEffect(() => {
    const handleRedirect = async () => {
      const token = searchParams.get('token');
      const error = searchParams.get('error');

      if (error) {
        setErrorMsg('Authentication failed. Please try again.');
        setTimeout(() => navigate('/login'), 3000);
        return;
      }

      if (token) {
        const result = await loginWithToken(token);
        if (result.success) {
          navigate('/dashboard');
        } else {
          setErrorMsg(result.error || 'Failed to fetch user profile.');
          setTimeout(() => navigate('/login'), 3000);
        }
      } else {
        navigate('/login');
      }
    };

    handleRedirect();
  }, [searchParams, navigate, loginWithToken]);

  return (
    <div className="flex-center page-wrapper" style={{ backgroundColor: 'var(--bg-primary)' }}>
      <div className="glass-panel animate-slide-up" style={{ padding: '2.5rem', textAlign: 'center', maxWidth: '400px', width: '100%' }}>
        {errorMsg ? (
          <div>
            <div style={{
              width: '4rem', height: '4rem',
              background: 'rgba(239, 68, 68, 0.1)',
              color: '#ef4444',
              borderRadius: '50%',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 1.25rem'
            }}>
              <svg width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>Authentication Failed</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>{errorMsg}</p>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Redirecting back to login...</p>
          </div>
        ) : (
          <div>
            <Loader2
              size={48}
              className="animate-spin"
              style={{ color: 'var(--brand-primary)', margin: '0 auto 1.25rem', display: 'block' }}
            />
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>Authenticating...</h2>
            <p style={{ color: 'var(--text-secondary)' }}>Please wait while we log you in.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;
