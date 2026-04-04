import React, { useState, useEffect } from 'react';
import { Save, Loader2, Building, Clock, MapPin, Briefcase } from 'lucide-react';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';

const OrganizationSettings = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    organizationType: 'COMPANY',
    email: '',
    phone: '',
    website: '',
    timezone: 'UTC',
    workingDays: 'MON,TUE,WED,THU,FRI',
    workingHoursStart: '08:00',
    workingHoursEnd: '18:00',
    slotDurationMinutes: 60,
    settings: {}
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchOrganization();
  }, []);

  const fetchOrganization = async () => {
    try {
      setIsLoading(true);
      const { data } = await api.get('/api/v1/organizations/me');
      if (data.success) {
        setFormData({
            ...data.data,
            settings: data.data.settings || {}
        });
      }
    } catch (err) {
      console.error("Failed to fetch organization", err);
      setMessage({ type: 'error', text: 'Failed to load organization details.' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSettingChange = (e) => {
    setFormData({
      ...formData,
      settings: {
        ...formData.settings,
        [e.target.name]: e.target.value
      }
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    setMessage({ type: '', text: '' });
    
    try {
      const { data } = await api.put('/api/v1/organizations/me', formData);
      if (data.success) {
        setMessage({ type: 'success', text: 'Organization details updated successfully!' });
      } else {
        setMessage({ type: 'error', text: data.message || 'Failed to update organization.' });
      }
    } catch (err) {
      console.error(err);
      setMessage({ type: 'error', text: 'An error occurred while saving.' });
    } finally {
      setIsSaving(false);
    }
  };

  const renderTypeSpecificFields = () => {
    switch (formData.organizationType) {
      case 'SCHOOL':
      case 'COLLEGE':
      case 'UNIVERSITY':
        return (
          <>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginTop: '2rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>Academic Settings</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="input-group">
                <label className="input-label">Academic Year</label>
                <input type="text" name="academicYear" placeholder="e.g. 2026-2027" className="input-field" value={formData.settings.academicYear || ''} onChange={handleSettingChange} />
              </div>
              <div className="input-group">
                <label className="input-label">Grading System</label>
                <select name="gradingSystem" className="input-field" value={formData.settings.gradingSystem || ''} onChange={handleSettingChange}>
                    <option value="">Select System</option>
                    <option value="GPA">GPA (4.0 Scale)</option>
                    <option value="PERCENTAGE">Percentage (0-100%)</option>
                    <option value="LETTER">Letter Grades (A-F)</option>
                </select>
              </div>
              <div className="input-group">
                <label className="input-label">Semester Type</label>
                <select name="semesterType" className="input-field" value={formData.settings.semesterType || ''} onChange={handleSettingChange}>
                    <option value="">Select Type</option>
                    <option value="SEMESTER">Semesters (2/year)</option>
                    <option value="TRIMESTER">Trimesters (3/year)</option>
                    <option value="QUARTER">Quarters (4/year)</option>
                </select>
              </div>
            </div>
          </>
        );
      case 'HOSPITAL':
        return (
          <>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginTop: '2rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>Hospital Settings</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="input-group">
                <label className="input-label">Bed Capacity</label>
                <input type="number" name="bedCapacity" className="input-field" value={formData.settings.bedCapacity || ''} onChange={handleSettingChange} />
              </div>
              <div className="input-group">
                <label className="input-label">Emergency Services</label>
                <select name="hasEmergencyServices" className="input-field" value={formData.settings.hasEmergencyServices || 'NO'} onChange={handleSettingChange}>
                    <option value="YES">Yes, 24/7 Available</option>
                    <option value="NO">No Emergency Services</option>
                </select>
              </div>
            </div>
          </>
        );
      case 'COMPANY':
        return (
          <>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginTop: '2rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>Company Details</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="input-group">
                <label className="input-label">Industry Type</label>
                <input type="text" name="industry" placeholder="e.g. Technology, Software" className="input-field" value={formData.settings.industry || ''} onChange={handleSettingChange} />
              </div>
              <div className="input-group">
                <label className="input-label">Registration Number</label>
                <input type="text" name="registrationNumber" className="input-field" value={formData.settings.registrationNumber || ''} onChange={handleSettingChange} />
              </div>
            </div>
          </>
        );
      default:
        return null;
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <Loader2 className="animate-spin text-brand" size={32} />
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto', width: '100%' }} className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Organization Settings</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage your organization details and operational preferences.</p>
        </div>
        <button onClick={handleSave} disabled={isSaving} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          {isSaving ? <Loader2 size={16} className="animate-spin" /> : <Save size={16} />}
          Save Changes
        </button>
      </div>

      {message.text && (
        <div style={{ 
            padding: '1rem', 
            borderRadius: 'var(--radius-sm)', 
            marginBottom: '1.5rem',
            backgroundColor: message.type === 'success' ? 'rgba(16, 217, 160, 0.1)' : 'rgba(239, 68, 68, 0.1)',
            color: message.type === 'success' ? '#10d9a0' : '#ef4444',
            border: `1px solid ${message.type === 'success' ? 'rgba(16, 217, 160, 0.2)' : 'rgba(239, 68, 68, 0.2)'}`
        }}>
          {message.text}
        </div>
      )}

      <div className="glass-panel" style={{ padding: '2.5rem' }}>
        <form id="org-settings-form">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
             <Building size={18} className="text-brand" /> Core Information
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '2rem' }}>
            <div className="input-group" style={{ gridColumn: 'span 2' }}>
              <label className="input-label">Organization Name</label>
              <input type="text" name="name" required className="input-field" value={formData.name} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label className="input-label">Organization Type</label>
              <select name="organizationType" className="input-field" value={formData.organizationType} onChange={handleChange}>
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
            <div className="input-group">
              <label className="input-label">Email Contacts</label>
              <input type="email" name="email" className="input-field" value={formData.email || ''} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label className="input-label">Phone Support</label>
              <input type="text" name="phone" className="input-field" value={formData.phone || ''} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label className="input-label">Website</label>
              <input type="url" name="website" className="input-field" value={formData.website || ''} onChange={handleChange} />
            </div>
          </div>

          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
             <Clock size={18} className="text-brand" /> Operation Settings
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Timezone</label>
              <input type="text" name="timezone" className="input-field" value={formData.timezone} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label className="input-label">Slot Duration (Minutes)</label>
              <input type="number" name="slotDurationMinutes" min="5" step="5" className="input-field" value={formData.slotDurationMinutes} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label className="input-label">Operating Days</label>
              <input type="text" name="workingDays" placeholder="MON,TUE,WED,THU,FRI" className="input-field" value={formData.workingDays} onChange={handleChange} />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                <div className="input-group">
                    <label className="input-label">Start Time</label>
                    <input type="time" name="workingHoursStart" className="input-field" value={formData.workingHoursStart} onChange={handleChange} />
                </div>
                <div className="input-group">
                    <label className="input-label">End Time</label>
                    <input type="time" name="workingHoursEnd" className="input-field" value={formData.workingHoursEnd} onChange={handleChange} />
                </div>
            </div>
          </div>

          {/* Type-specific Fields Rendered Dynamically */}
          {renderTypeSpecificFields()}

        </form>
      </div>
    </div>
  );
};

export default OrganizationSettings;
