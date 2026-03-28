import axios from 'axios';

const API_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach the JWT Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401s (Token Expiry)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Optional: Implement refresh token logic here if a 401 occurs.
    if (error.response?.status === 401 && !originalRequest._retry) {
      /* 
       For this chunk, we simply wipe and redirect if unauth
       In the future, hit /api/v1/auth/refresh-token here!
      */
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.dispatchEvent(new Event('auth-expired'));
    }

    return Promise.reject(error);
  }
);

export default api;
