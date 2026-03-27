import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Calendar, Mail, Lock, User, Loader2, Building } from 'lucide-react';

const Signup = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    organizationName: '',
    organizationType: 'COMPANY'
  });
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

  const handleSignup = async (e) => {
    e.preventDefault();
    if(formData.password !== formData.confirmPassword) {
        return setError("Passwords do not match!");
    }
    
    setIsLoading(true);
    setError(null);
    
    const result = await signup(formData);
    
    setIsLoading(false);
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
  };

  return (
    <div className="flex-center page-wrapper animate-fade-in" style={{ backgroundColor: 'var(--bg-primary)' }}>
      <div style={{ position: 'absolute', top: '2rem', left: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none', color: 'inherit' }}>
          <div style={{ background: 'var(--brand-gradient)', padding: '0.5rem', borderRadius: 'var(--radius-sm)' }}>
            <Calendar color="white" size={20} />
          </div>
          <span style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>Schedulo</span>
        </Link>
      </div>

      <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '500px', padding: '2.5rem', margin: '4rem 0' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.75rem' }}>Create Account</h2>
          <p style={{ color: 'var(--text-secondary)' }}>Get started with intelligent scheduling</p>
        </div>

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSignup}>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className="input-group" style={{ flex: 1 }}>
              <label className="input-label">First Name</label>
              <div style={{ position: 'relative' }}>
                <User style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
                <input type="text" name="firstName" required className="input-field" style={{ paddingLeft: '2.5rem' }} value={formData.firstName} onChange={handleChange} />
              </div>
            </div>
            <div className="input-group" style={{ flex: 1 }}>
              <label className="input-label">Last Name</label>
              <input type="text" name="lastName" required className="input-field" value={formData.lastName} onChange={handleChange} />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
              <input type="email" name="email" required className="input-field" style={{ paddingLeft: '2.5rem' }} value={formData.email} onChange={handleChange} />
            </div>
          </div>
          
          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className="input-group" style={{ flex: 2 }}>
              <label className="input-label">Organization Name (Optional)</label>
              <div style={{ position: 'relative' }}>
                <Building style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
                <input type="text" name="organizationName" className="input-field" style={{ paddingLeft: '2.5rem' }} placeholder="Your Company Ltd" value={formData.organizationName} onChange={handleChange} />
              </div>
            </div>
            
            <div className="input-group" style={{ flex: 1 }}>
              <label className="input-label">Type</label>
              <select name="organizationType" className="input-field" value={formData.organizationType} onChange={handleChange} style={{ cursor: 'pointer' }}>
                <option value="COMPANY">Company</option>
                <option value="HOSPITAL">Hospital</option>
                <option value="SCHOOL">School</option>
                <option value="COLLEGE">College</option>
                <option value="UNIVERSITY">University</option>
                <option value="NON_PROFIT">Non-Profit</option>
                <option value="GOVERNMENT">Government</option>
                <option value="INDIVIDUAL">Individual</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
              <input type="password" name="password" required minLength="8" className="input-field" style={{ paddingLeft: '2.5rem' }} value={formData.password} onChange={handleChange} />
            </div>
          </div>
          
          <div className="input-group">
            <label className="input-label">Confirm Password</label>
            <div style={{ position: 'relative' }}>
              <Lock style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} size={18} />
              <input type="password" name="confirmPassword" required minLength="8" className="input-field" style={{ paddingLeft: '2.5rem' }} value={formData.confirmPassword} onChange={handleChange} />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={isLoading}>
            {isLoading ? <Loader2 className="animate-spin" size={20} /> : 'Create Account'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--brand-primary)', textDecoration: 'none', fontWeight: '500' }}>Sign in</Link>
        </div>
      </div>
    </div>
  );
};

export default Signup;
