import React, { useEffect, useState, useCallback } from 'react';
import api from '../../services/api';
import { Layers, Plus, Search, Edit2, Trash2, X, Loader2, ChevronRight, Users, Tag } from 'lucide-react';

const COLOR_SWATCHES = ['#4f8ef7', '#9b72f7', '#10d9a0', '#f7a94f', '#f74f6e', '#06b6d4', '#84cc16', '#f59e0b'];

const DepartmentModal = ({ dept, onClose, onSave, allDepts }) => {
  const [form, setForm] = useState({
    name: dept?.name || '',
    code: dept?.code || '',
    description: dept?.description || '',
    color: dept?.color || COLOR_SWATCHES[0],
    sortOrder: dept?.sortOrder ?? 0,
    parentDepartmentId: dept?.parentDepartmentId || '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        sortOrder: parseInt(form.sortOrder) || 0,
        parentDepartmentId: form.parentDepartmentId || null,
      };
      if (dept?.id) {
        await api.put(`/api/v1/departments/${dept.id}`, payload);
      } else {
        await api.post('/api/v1/departments', payload);
      }
      onSave();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save department');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(6px)' }}>
      <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '520px', margin: '1rem', padding: '2rem', position: 'relative' }}>
        <button onClick={onClose} style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}>
          <X size={20} />
        </button>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.25rem' }}>
          {dept?.id ? 'Edit Department' : 'New Department'}
        </h3>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
          {dept?.id ? 'Update department details.' : 'Add a new department to your organization.'}
        </p>

        {error && (
          <div style={{ padding: '0.75rem 1rem', background: 'rgba(247,79,110,0.1)', border: '1px solid rgba(247,79,110,0.3)', borderRadius: 'var(--radius-sm)', color: 'var(--brand-danger)', fontSize: '0.85rem', marginBottom: '1rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
            <div className="input-group" style={{ gridColumn: 'span 2' }}>
              <label className="input-label">Department Name *</label>
              <input type="text" name="name" required className="input-field" value={form.name} onChange={handleChange} placeholder="e.g. Computer Science" />
            </div>
            <div className="input-group">
              <label className="input-label">Short Code</label>
              <input type="text" name="code" className="input-field" value={form.code} onChange={handleChange} placeholder="e.g. CS" maxLength={10} />
            </div>
            <div className="input-group">
              <label className="input-label">Sort Order</label>
              <input type="number" name="sortOrder" className="input-field" value={form.sortOrder} onChange={handleChange} min={0} />
            </div>
          </div>

          <div className="input-group" style={{ marginBottom: '1rem' }}>
            <label className="input-label">Description</label>
            <textarea name="description" className="input-field" rows={2} value={form.description} onChange={handleChange} style={{ resize: 'vertical' }} />
          </div>

          {allDepts && allDepts.length > 0 && (
            <div className="input-group" style={{ marginBottom: '1rem' }}>
              <label className="input-label">Parent Department</label>
              <select name="parentDepartmentId" className="input-field" value={form.parentDepartmentId} onChange={handleChange}>
                <option value="">None (Top-Level)</option>
                {allDepts.filter(d => d.id !== dept?.id).map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </div>
          )}

          <div className="input-group" style={{ marginBottom: '1.5rem' }}>
            <label className="input-label">Color Tag</label>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '0.5rem' }}>
              {COLOR_SWATCHES.map(color => (
                <button
                  key={color}
                  type="button"
                  onClick={() => setForm({ ...form, color })}
                  style={{
                    width: '28px', height: '28px', borderRadius: '50%', background: color, border: 'none',
                    cursor: 'pointer', outline: form.color === color ? `2px solid white` : 'none',
                    outlineOffset: '2px', boxShadow: form.color === color ? `0 0 8px ${color}` : 'none',
                    transition: 'all 0.2s'
                  }}
                />
              ))}
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              {saving ? <Loader2 size={16} className="animate-spin" /> : <Plus size={16} />}
              {dept?.id ? 'Save Changes' : 'Create Department'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const DepartmentCard = ({ dept, onEdit, onDelete }) => {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className="glass-card" style={{ padding: '1.25rem', borderLeft: `3px solid ${dept.color || 'var(--brand-primary)'}`, transition: 'all 0.2s' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ width: '40px', height: '40px', borderRadius: 'var(--radius-sm)', background: dept.color || 'var(--brand-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
          <Layers size={18} color="white" />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.15rem' }}>
            <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>{dept.name}</span>
            {dept.code && (
              <span style={{ background: 'rgba(255,255,255,0.08)', padding: '0.1rem 0.5rem', borderRadius: '0.75rem', fontSize: '0.7rem', fontWeight: 600, color: 'var(--text-muted)' }}>{dept.code}</span>
            )}
          </div>
          {dept.description && <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{dept.description}</p>}
          {dept.headName && (
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.15rem' }}>
              <Users size={11} /> Head: {dept.headName}
            </div>
          )}
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', flexShrink: 0 }}>
          {dept.subDepartments && dept.subDepartments.length > 0 && (
            <button onClick={() => setExpanded(!expanded)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem' }}>
              <ChevronRight size={14} style={{ transform: expanded ? 'rotate(90deg)' : 'none', transition: 'transform 0.2s' }} />
              {dept.subDepartments.length}
            </button>
          )}
          <button onClick={() => onEdit(dept)} style={{ background: 'rgba(79,142,247,0.1)', border: '1px solid rgba(79,142,247,0.25)', borderRadius: 'var(--radius-sm)', color: 'var(--brand-primary)', cursor: 'pointer', padding: '0.35rem 0.65rem', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Edit2 size={13} /> Edit
          </button>
          <button onClick={() => onDelete(dept)} style={{ background: 'rgba(247,79,110,0.1)', border: '1px solid rgba(247,79,110,0.25)', borderRadius: 'var(--radius-sm)', color: 'var(--brand-danger)', cursor: 'pointer', padding: '0.35rem 0.65rem', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Trash2 size={13} />
          </button>
        </div>
      </div>

      {expanded && dept.subDepartments && dept.subDepartments.length > 0 && (
        <div style={{ marginTop: '0.75rem', paddingLeft: '1rem', borderLeft: '2px solid var(--border-color)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          {dept.subDepartments.map(sub => (
            <div key={sub.id} style={{ padding: '0.6rem 0.75rem', background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: sub.color || 'var(--brand-secondary)', flexShrink: 0 }} />
              <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{sub.name}</span>
              {sub.code && <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{sub.code}</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const Departments = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modalDept, setModalDept] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchDepartments = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/v1/departments');
      setDepartments(res.data?.data || []);
    } catch {
      showToast('Failed to load departments', 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchDepartments(); }, [fetchDepartments]);

  const handleCreate = () => { setModalDept(null); setShowModal(true); };
  const handleEdit = (dept) => { setModalDept(dept); setShowModal(true); };
  const handleModalClose = () => { setShowModal(false); setModalDept(null); };
  const handleModalSave = () => {
    handleModalClose();
    showToast(modalDept?.id ? 'Department updated!' : 'Department created!');
    fetchDepartments();
  };

  const handleDelete = async (dept) => {
    if (!window.confirm(`Delete "${dept.name}"? This action cannot be undone.`)) return;
    try {
      await api.delete(`/api/v1/departments/${dept.id}`);
      showToast('Department deleted');
      fetchDepartments();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete department', 'error');
    }
  };

  const filtered = departments.filter(d =>
    `${d.name} ${d.code || ''} ${d.description || ''}`.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }} className="animate-fade-in">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', margin: 0, marginBottom: '0.25rem', fontWeight: 700 }}>Departments</h1>
          <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            {departments.length} department{departments.length !== 1 ? 's' : ''} in your organization
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            <Search size={15} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              className="input-field"
              placeholder="Search departments..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              style={{ paddingLeft: '2.25rem', width: '220px', fontSize: '0.875rem' }}
            />
          </div>
          <button className="btn btn-primary" onClick={handleCreate} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Plus size={16} /> Add Department
          </button>
        </div>
      </div>

      {/* Content */}
      {loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {[1, 2, 3].map(i => <div key={i} className="skeleton" style={{ height: '80px', borderRadius: 'var(--radius-md)' }} />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="glass-card empty-state">
          <div className="empty-icon"><Layers size={32} color="var(--brand-primary)" /></div>
          <h3>{search ? 'No departments found' : 'No Departments Yet'}</h3>
          <p>{search ? 'Try a different search term.' : 'Create your first department to organize your team and schedules.'}</p>
          {!search && (
            <button className="btn btn-primary" onClick={handleCreate} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Plus size={16} /> Create First Department
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {filtered.map(dept => (
            <DepartmentCard key={dept.id} dept={dept} onEdit={handleEdit} onDelete={handleDelete} />
          ))}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <DepartmentModal
          dept={modalDept}
          allDepts={departments}
          onClose={handleModalClose}
          onSave={handleModalSave}
        />
      )}

      {/* Toast */}
      {toast && (
        <div className="toast-container">
          <div className={`toast toast-${toast.type}`}>{toast.msg}</div>
        </div>
      )}
    </div>
  );
};

export default Departments;
