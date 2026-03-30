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
    <div className="min-h-screen flex items-center justify-center bg-[#0a0f1c]">
      <div className="card p-8 text-center max-w-md mx-auto">
        {errorMsg ? (
          <div>
            <div className="w-16 h-16 bg-red-500/10 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-xl font-bold text-white mb-2">Authentication Failed</h2>
            <p className="text-[#8b8fa3]">{errorMsg}</p>
            <p className="text-[#8b8fa3] text-sm mt-4">Redirecting back to login...</p>
          </div>
        ) : (
          <div>
            <Loader2 className="w-12 h-12 text-[#10d9a0] animate-spin mx-auto mb-4" />
            <h2 className="text-xl font-bold text-white mb-2">Authenticating...</h2>
            <p className="text-[#8b8fa3]">Please wait while we log you in.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;
