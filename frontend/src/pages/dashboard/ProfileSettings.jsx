import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { User, Mail, Phone, MapPin, Camera, Save, Lock, Shield } from 'lucide-react';
import api from '../../services/api';

const ProfileSettings = () => {
  const { user, login } = useAuth();
  const [activeTab, setActiveTab] = useState('personal');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    timezone: 'UTC',
    profileImageUrl: ''
  });

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phoneNumber: user.phoneNumber || '',
        timezone: user.timezone || 'UTC',
        profileImageUrl: user.profileImageUrl || ''
      });
    }
  }, [user]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });
    
    try {
      const response = await api.put(`/users/${user.id}`, {
        ...formData,
        roles: user.roles // preserve existing roles
      });
      setMessage({ type: 'success', text: 'Profile updated successfully!' });
      
      // We need to refresh the JWT to get the new payload, 
      // but Since we don't have a direct /refresh endpoint hooked up, 
      // we can inform the user.
      setTimeout(() => setMessage({ type: '', text: '' }), 5000);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update profile.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-wrapper" style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.5rem 0' }}>Profile Settings</h1>
        <p style={{ color: 'var(--text-muted)', margin: 0 }}>Manage your personal information and security preferences.</p>
      </div>

      <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap', alignItems: 'flex-start' }}>
        
        {/* Left Sidebar Menu */}
        <div style={{ flex: '0 0 240px', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <button 
            onClick={() => setActiveTab('personal')}
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '1rem', background: activeTab === 'personal' ? 'var(--bg-secondary)' : 'transparent', border: 'none', borderRadius: 'var(--radius-md)', fontWeight: activeTab === 'personal' ? 600 : 500, color: activeTab === 'personal' ? 'var(--brand-primary)' : 'var(--text-primary)', cursor: 'pointer', textAlign: 'left', transition: 'all 0.2s', borderLeft: activeTab === 'personal' ? '3px solid var(--brand-primary)' : '3px solid transparent' }}
          >
            <User size={18} /> Personal Info
          </button>
          
          <button 
            onClick={() => setActiveTab('security')}
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '1rem', background: activeTab === 'security' ? 'var(--bg-secondary)' : 'transparent', border: 'none', borderRadius: 'var(--radius-md)', fontWeight: activeTab === 'security' ? 600 : 500, color: activeTab === 'security' ? 'var(--brand-primary)' : 'var(--text-primary)', cursor: 'pointer', textAlign: 'left', transition: 'all 0.2s', borderLeft: activeTab === 'security' ? '3px solid var(--brand-primary)' : '3px solid transparent' }}
          >
            <Shield size={18} /> Security
          </button>
        </div>

        {/* Right Content Area */}
        <div style={{ flex: '1', minWidth: '300px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', padding: '2rem' }}>
          
          {message.text && (
            <div style={{ padding: '1rem', marginBottom: '1.5rem', borderRadius: 'var(--radius-md)', background: message.type === 'success' ? 'rgba(16, 217, 160, 0.1)' : 'rgba(239, 68, 68, 0.1)', color: message.type === 'success' ? 'var(--brand-success)' : 'var(--brand-danger)', border: `1px solid ${message.type === 'success' ? 'rgba(16, 217, 160, 0.2)' : 'rgba(239, 68, 68, 0.2)'}` }}>
              {message.text}
            </div>
          )}

          {activeTab === 'personal' && (
            <form onSubmit={handleSubmit}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginBottom: '2rem', paddingBottom: '2rem', borderBottom: '1px solid var(--border-color)' }}>
                <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'var(--brand-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem', fontWeight: 600, letterSpacing: '-1px' }}>
                   {user?.firstName?.charAt(0) || ''}{user?.lastName?.charAt(0) || ''}
                </div>
                <div>
                  <button type="button" className="btn btn-secondary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <Camera size={16} /> Upload New Photo
                  </button>
                  <p style={{ margin: '0.5rem 0 0', fontSize: '0.75rem', color: 'var(--text-muted)' }}>At least 256x256 PNG or JPG.</p>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
                <div className="input-group">
                  <label className="input-label">First Name</label>
                  <input type="text" className="input-field" name="firstName" value={formData.firstName} onChange={handleInputChange} required />
                </div>
                <div className="input-group">
                  <label className="input-label">Last Name</label>
                  <input type="text" className="input-field" name="lastName" value={formData.lastName} onChange={handleInputChange} required />
                </div>
              </div>

              <div className="input-group">
                <label className="input-label">Email Address <span style={{ color: 'var(--text-muted)', fontWeight: 400, marginLeft: '0.5rem', fontSize: '0.78rem' }}>(Read Only)</span></label>
                <input type="email" className="input-field" value={user?.email || ''} readOnly style={{ opacity: 0.6, cursor: 'not-allowed' }} />
              </div>

              <div className="input-group">
                <label className="input-label">Phone Number</label>
                <input type="tel" className="input-field" name="phoneNumber" value={formData.phoneNumber || ''} onChange={handleInputChange} placeholder="+1 (555) 000-0000" />
              </div>

              <div className="input-group" style={{ marginBottom: '2rem' }}>
                <label className="input-label">Timezone</label>
                <select className="input-field" name="timezone" value={formData.timezone} onChange={handleInputChange} style={{ cursor: 'pointer' }}>
                  <option value="UTC">UTC (Universal Time)</option>
                  <option value="Asia/Kolkata">India (IST +5:30)</option>
                  <option value="America/New_York">Eastern Time (ET)</option>
                  <option value="America/Chicago">Central Time (CT)</option>
                  <option value="America/Denver">Mountain Time (MT)</option>
                  <option value="America/Los_Angeles">Pacific Time (PT)</option>
                  <option value="Europe/London">London (GMT)</option>
                  <option value="Asia/Tokyo">Tokyo (JST)</option>
                  <option value="Asia/Dubai">Dubai (GST +4)</option>
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: '1.5rem', borderTop: '1px solid var(--border-color)' }}>
                <button type="submit" disabled={loading} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Save size={18} /> {loading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          )}

          {activeTab === 'security' && (
            <div>
              <h3 style={{ margin: '0 0 1.5rem', fontSize: '1.1rem' }}>Change Password</h3>
              <form onSubmit={(e) => { e.preventDefault(); alert("Password change workflow is disabled for demo."); }}>
                <div className="input-group">
                  <label className="input-label">Current Password</label>
                  <input type="password" className="input-field" placeholder="••••••••" />
                </div>

                <div className="input-group">
                  <label className="input-label">New Password</label>
                  <input type="password" className="input-field" placeholder="••••••••" />
                </div>

                <div className="input-group" style={{ marginBottom: '2rem' }}>
                  <label className="input-label">Confirm New Password</label>
                  <input type="password" className="input-field" placeholder="••••••••" />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: '1.5rem', borderTop: '1px solid var(--border-color)' }}>
                  <button type="submit" className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Lock size={18} /> Update Password
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfileSettings;
