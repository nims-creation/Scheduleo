import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // Lazy initializer avoids calling setState synchronously inside an effect
  const [user, setUser] = useState(() => {
    try {
      const storedUser = localStorage.getItem('user');
      const token = localStorage.getItem('accessToken');
      return storedUser && token ? JSON.parse(storedUser) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(false);

  // Listen for 401s
  useEffect(() => {
    const handleAuthExpired = () => setUser(null);
    window.addEventListener('auth-expired', handleAuthExpired);
    return () => window.removeEventListener('auth-expired', handleAuthExpired);
  }, []);

  const login = async (email, password) => {
    try {
      const { data } = await api.post('/api/v1/auth/login', { email, password });
      if (data.success) {
        const { accessToken, refreshToken, user: userData } = data.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        return { success: true };
      }
      return { success: false, error: data.message };
    } catch (err) {
      let errorMsg = 'Failed to login';
      if (err.response?.data) {
        const { message, error } = err.response.data;
        if (error?.details && typeof error.details === 'object' && Object.keys(error.details).length > 0) {
          errorMsg = Object.values(error.details)[0];
        } else if (error?.description) {
          errorMsg = error.description;
        } else if (message) {
          errorMsg = message;
        }
      }
      return { success: false, error: errorMsg };
    }
  };

  const loginWithToken = async (token) => {
    try {
      localStorage.setItem('accessToken', token);
      
      // Fetch user profile
      const { data } = await api.get('/api/v1/users/me', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      if (data.success) {
        const userData = data.data;
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        return { success: true };
      }
      return { success: false, error: data.message };
    } catch (err) {
      console.error('OAuth token validation failed:', err);
      localStorage.removeItem('accessToken');
      return { success: false, error: 'Failed to process OAuth token' };
    }
  };

  const signup = async (userData) => {
    try {
      // Schedulo API payload standard for creating accounts
      const payload = { ...userData, timezone: Intl.DateTimeFormat().resolvedOptions().timeZone };
      const { data } = await api.post('/api/v1/auth/signup', payload);
      if (data.success) {
        const { accessToken, refreshToken, user: newUser } = data.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(newUser));
        setUser(newUser);
        return { success: true };
      }
      return { success: false, error: data.message };
    } catch (err) {
      let errorMsg = 'Failed to register';
      if (err.response?.data) {
        const { message, error } = err.response.data;
        if (error?.details && typeof error.details === 'object' && Object.keys(error.details).length > 0) {
          errorMsg = Object.values(error.details)[0];
        } else if (error?.description) {
          errorMsg = error.description;
        } else if (message) {
          errorMsg = message;
        }
      }
      return { success: false, error: errorMsg };
    }
  };

  const logout = () => {
    // Optionally hit /api/v1/auth/logout with refreshToken here
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, loginWithToken, signup, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

// Exported separately to satisfy react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
