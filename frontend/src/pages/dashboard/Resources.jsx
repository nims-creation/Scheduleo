import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit, Building2, Server, Monitor, Truck } from 'lucide-react';
import api from '../../services/api';

const Resources = () => {
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingResource, setEditingResource] = useState(null);
  const [error, setError] = useState(null);
  
  const [formData, setFormData] = useState({
    name: '', type: 'ROOM', capacity: '', isAvailableForBooking: true
  });

  const fetchResources = async () => {
    try {
      setLoading(true);
      const { data } = await api.get('/api/v1/resources');
      setResources(data.data || []);
    } catch {
      // silently ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchResources(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const payload = { ...formData };
      if (payload.capacity === '') {
        payload.capacity = null;
      }
      
      if (editingResource) {
        await api.put(`/api/v1/resources/${editingResource.id}`, payload);
      } else {
        await api.post('/api/v1/resources', payload);
      }
      setShowModal(false);
      fetchResources();
    } catch (err) {
      let errorText = err.response?.data?.message || err.message;
      if (err.response?.data?.error?.details) {
        const details = err.response.data.error.details;
        if (typeof details === 'object' && Object.keys(details).length > 0) {
          errorText = Object.values(details)[0];
        }
      }
      setError('Error saving resource: ' + errorText);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this resource?')) {
      try {
        await api.delete(`/api/v1/resources/${id}`);
        fetchResources();
      } catch {
        alert('Failed to delete resource');
      }
    }
  };

  const getIconForType = (type) => {
    switch (type) {
      case 'ROOM': return <Building2 size={18} />;
      case 'LAB': return <Server size={18} />;
      case 'EQUIPMENT': return <Monitor size={18} />;
      case 'VEHICLE': return <Truck size={18} />;
      default: return <Building2 size={18} />;
    }
  };

  return (
    <div className="page-wrapper" style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.5rem 0' }}>Resources</h1>
          <p style={{ color: 'var(--text-muted)', margin: 0 }}>Manage rooms, labs, and equipment for your organization.</p>
        </div>
        <button className="btn btn-primary" onClick={() => {
          setEditingResource(null);
          setError(null);
          setFormData({ name: '', type: 'ROOM', capacity: '', isAvailableForBooking: true });
          setShowModal(true);
        }}>
          <Plus size={16} /> Add Resource
        </button>
      </div>

      <div style={{ background: 'var(--bg-secondary)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', background: 'rgba(0,0,0,0.2)' }}>
              <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Name</th>
              <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Type</th>
              <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Capacity</th>
              <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status</th>
              <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)', textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="5" style={{ padding: '2rem', textAlign: 'center' }}>Loading...</td></tr>
            ) : resources.length === 0 ? (
              <tr><td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No resources found. Add one to get started.</td></tr>
            ) : (
              resources.map(res => (
                <tr key={res.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem 1.5rem', fontWeight: 500 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <div style={{ padding: '0.5rem', background: 'rgba(79, 142, 247, 0.1)', color: 'var(--brand-primary)', borderRadius: '8px' }}>
                        {getIconForType(res.type)}
                      </div>
                      {res.name}
                    </div>
                  </td>
                  <td style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)' }}>{res.type}</td>
                  <td style={{ padding: '1rem 1.5rem' }}>{res.capacity || 'N/A'}</td>
                  <td style={{ padding: '1rem 1.5rem' }}>
                    <span style={{ 
                      padding: '0.25rem 0.75rem', borderRadius: '1rem', fontSize: '0.75rem', fontWeight: 600,
                      background: res.isAvailableForBooking ? 'rgba(16, 217, 160, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                      color: res.isAvailableForBooking ? 'var(--brand-success)' : 'var(--brand-danger)'
                    }}>
                      {res.isAvailableForBooking ? 'Available' : 'Unavailable'}
                    </span>
                  </td>
                  <td style={{ padding: '1rem 1.5rem', textAlign: 'right' }}>
                    <button className="btn btn-outline" style={{ padding: '0.5rem', marginRight: '0.5rem' }} onClick={() => {
                        setEditingResource(res);
                        setError(null);
                        setFormData({ name: res.name, type: res.type, capacity: res.capacity || '', isAvailableForBooking: res.isAvailableForBooking });
                        setShowModal(true);
                      }}>
                      <Edit size={16} />
                    </button>
                    <button className="btn btn-outline" style={{ padding: '0.5rem', color: 'var(--brand-danger)', borderColor: 'rgba(239, 68, 68, 0.2)' }} onClick={() => handleDelete(res.id)}>
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(6px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '460px', padding: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>{editingResource ? 'Edit Resource' : 'Add Resource'}</h3>
                <p style={{ margin: '0.25rem 0 0', color: 'var(--text-muted)', fontSize: '0.8rem' }}>Define rooms, labs, and equipment for scheduling</p>
              </div>
              <button
                onClick={() => setShowModal(false)}
                style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid var(--border-color)', color: 'var(--text-muted)', borderRadius: '8px', width: '32px', height: '32px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.1rem', transition: 'all 0.2s' }}
                onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.12)'}
                onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.07)'}
              >×</button>
            </div>

            <form onSubmit={handleSubmit}>
              {error && (
                <div style={{ background: 'rgba(239, 68, 68, 0.1)', borderLeft: '4px solid var(--brand-danger)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', color: 'var(--brand-danger)', fontSize: '0.9rem' }}>
                  {error}
                </div>
              )}
              <div className="input-group">
                <label className="input-label">Resource Name</label>
                <input
                  type="text"
                  className="input-field"
                  required
                  placeholder="e.g. Room 101, Chemistry Lab"
                  value={formData.name}
                  onChange={e => setFormData({...formData, name: e.target.value})}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="input-group">
                  <label className="input-label">Type</label>
                  <select
                    className="input-field"
                    value={formData.type}
                    onChange={e => setFormData({...formData, type: e.target.value})}
                    style={{ cursor: 'pointer' }}
                  >
                    <option value="ROOM">🏫 Room</option>
                    <option value="LAB">🔬 Lab</option>
                    <option value="EQUIPMENT">🖥️ Equipment</option>
                    <option value="VEHICLE">🚌 Vehicle</option>
                    <option value="OTHER">📦 Other</option>
                  </select>
                </div>
                <div className="input-group">
                  <label className="input-label">Capacity</label>
                  <input
                    type="number"
                    className="input-field"
                    placeholder="30"
                    value={formData.capacity}
                    onChange={e => setFormData({...formData, capacity: e.target.value ? parseInt(e.target.value) : ''})}
                  />
                </div>
              </div>

              {/* Available toggle */}
              <div
                onClick={() => setFormData({...formData, isAvailableForBooking: !formData.isAvailableForBooking})}
                style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '0.875rem 1rem', borderRadius: 'var(--radius-sm)',
                  border: `1px solid ${formData.isAvailableForBooking ? 'var(--brand-primary)' : 'var(--border-color)'}`,
                  background: formData.isAvailableForBooking ? 'rgba(16, 217, 160, 0.05)' : 'rgba(255,255,255,0.03)',
                  cursor: 'pointer', marginBottom: '1.5rem', transition: 'all 0.2s'
                }}
              >
                <div>
                  <div style={{ fontWeight: 500, fontSize: '0.9rem' }}>Available for Booking</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.78rem' }}>Can be assigned to events and schedules</div>
                </div>
                <div style={{
                  width: '42px', height: '24px', borderRadius: '12px', position: 'relative', transition: 'all 0.2s',
                  background: formData.isAvailableForBooking ? 'var(--brand-primary)' : 'rgba(255,255,255,0.1)'
                }}>
                  <div style={{
                    position: 'absolute', top: '3px', left: formData.isAvailableForBooking ? '21px' : '3px',
                    width: '18px', height: '18px', borderRadius: '50%', background: 'white', transition: 'all 0.2s',
                    boxShadow: '0 1px 4px rgba(0,0,0,0.3)'
                  }} />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>{editingResource ? 'Save Changes' : 'Create Resource'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Resources;
