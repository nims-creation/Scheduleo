import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import './index.css'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import ErrorBoundary from './components/ErrorBoundary.jsx'
import WakeupBanner from './components/WakeupBanner.jsx'
import { initServerWakeup } from './services/serverWakeup.js'

// Fire immediately on app boot — wakes the Render free-tier backend
// BEFORE the user ever clicks Login/Signup.
initServerWakeup();

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <ThemeProvider>
        <AuthProvider>
          <BrowserRouter>
            {/* Global wakeup status banner — shows only when server is cold */}
            <WakeupBanner />
            <App />
          </BrowserRouter>
        </AuthProvider>
      </ThemeProvider>
    </ErrorBoundary>
  </React.StrictMode>,
)
