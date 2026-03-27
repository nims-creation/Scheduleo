import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit, Building2, Server, Monitor, Truck } from 'lucide-react';
import api from '../../services/api';

const Resources = () => {
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingResource, setEditingResource] = useState(null);
  
  const [formData, setFormData] = useState({
    name: '', type: 'ROOM', capacity: '', isAvailableForBooking: true
  });

  const fetchResources = async () => {
    try {
      setLoading(true);
      const { data } = await api.get('/resources');
      setResources(data.data || []);
    } catch (error) {
      console.error('Failed to fetch resources:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchResources(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingResource) {
        await api.put(`/resources/${editingResource.id}`, formData);
      } else {
        await api.post('/resources', formData);
      }
      setShowModal(false);
      fetchResources();
    } catch (error) {
      alert('Error saving resource: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this resource?')) {
      try {
        await api.delete(`/resources/${id}`);
        fetchResources();
      } catch (error) {
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
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div style={{ background: 'var(--bg-primary)', padding: '2rem', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '400px', border: '1px solid var(--border-color)', boxShadow: '0 20px 40px rgba(0,0,0,0.2)' }}>
            <h3 style={{ margin: '0 0 1.5rem', display: 'flex', justifyContent: 'space-between' }}>
              {editingResource ? 'Edit Resource' : 'Add Resource'}
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '1.2rem' }}>×</button>
            </h3>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label>Name</label>
                <input type="text" className="form-control" required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} placeholder="Room 101" />
              </div>
              
              <div className="form-group">
                <label>Type</label>
                <select className="form-control" value={formData.type} onChange={e => setFormData({...formData, type: e.target.value})}>
                  <option value="ROOM">Room</option>
                  <option value="LAB">Lab</option>
                  <option value="EQUIPMENT">Equipment</option>
                  <option value="VEHICLE">Vehicle</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div className="form-group">
                <label>Capacity</label>
                <input type="number" className="form-control" value={formData.capacity} onChange={e => setFormData({...formData, capacity: e.target.value ? parseInt(e.target.value) : ''})} placeholder="30" />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
                <input type="checkbox" id="bookingStatus" checked={formData.isAvailableForBooking} onChange={e => setFormData({...formData, isAvailableForBooking: e.target.checked})} />
                <label htmlFor="bookingStatus" style={{ margin: 0 }}>Available for Booking?</label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editingResource ? 'Save Changes' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Resources;
