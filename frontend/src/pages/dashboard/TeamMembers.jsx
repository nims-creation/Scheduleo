import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { Users, Mail, Phone, Calendar, Plus, Search, Shield, X, CheckCircle, Upload } from 'lucide-react';

const ROLE_COLORS = { ADMIN: { bg: 'rgba(247,79,110,0.15)', color: 'var(--brand-danger)' }, MANAGER: { bg: 'rgba(79,142,247,0.15)', color: 'var(--brand-primary)' }, MEMBER: { bg: 'rgba(155,114,247,0.15)', color: 'var(--brand-secondary)' } };

const TeamMembers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [toast, setToast] = useState(null);
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [showBulkModal, setShowBulkModal] = useState(false);
  const [bulkInput, setBulkInput] = useState('');
  const [inviteData, setInviteData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'ROLE_USER'
  });
  const [inviting, setInviting] = useState(false);
  const { user: currentUser, logout } = useAuth();

  const showToast = (msg, type='success') => { setToast({msg,type}); setTimeout(()=>setToast(null),3000); };

  const fetchTeam = () => {
    setLoading(true);
    api.get('/api/v1/users')
      .then(res => {
        const list = res.data?.data?.content || res.data?.data || [];
        setUsers(Array.isArray(list) ? list : []);
      })
      .catch((err) => {
        let msg = err.response?.data?.message || err.message;
        if (msg.includes('No static') || err.response?.status === 500 || err.response?.status === 400 || msg === 'Network Error') {
           setError('Your session is stale because you were assigned an organization in the background. Please click the button below to refresh your session!');
        } else {
           setError('Failed to load team members: ' + msg);
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchTeam();
  }, []);

  const handleInviteChange = (e) => setInviteData({ ...inviteData, [e.target.name]: e.target.value });

  const handleInviteSubmit = async (e) => {
    e.preventDefault();
    setInviting(true);
    try {
      const payload = {
        ...inviteData,
        roles: [inviteData.role],
        organizationId: currentUser?.organization?.id,
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      };
      
      const res = await api.post('/api/v1/users', payload);
      if (res.data?.success) {
        showToast('Team member invited successfully!');
        setShowInviteModal(false);
        setInviteData({ firstName: '', lastName: '', email: '', password: '', role: 'ROLE_USER' });
        fetchTeam();
      } else {
        showToast(res.data?.message || 'Failed to invite user', 'error');
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'An error occurred during invitation', 'error');
    } finally {
      setInviting(false);
    }
  };

  const handleBulkSubmit = async (e) => {
    e.preventDefault();
    if (!bulkInput.trim()) return;
    setInviting(true);
    
    try {
      let requests = [];
      const lines = bulkInput.split('\n').map(l => l.trim()).filter(Boolean);
      
      lines.forEach((line) => {
         if (line.includes(',')) {
             const parts = line.split(',').map(p => p.trim());
             if (parts.length > 0) {
                 if (parts[1] && parts[1].includes('@')) {
                     // Comma-separated emails on one line
                     parts.forEach((p) => {
                         if (p) requests.push({ email: p, firstName: 'Student', lastName: `${requests.length + 1}`, password: 'Welcome123!', roles: ['ROLE_USER'], organizationId: currentUser?.organization?.id, timezone: Intl.DateTimeFormat().resolvedOptions().timeZone });
                     });
                 } else {
                     // CSV formatted: Email, FirstName, LastName
                     requests.push({ email: parts[0], firstName: parts[1] || 'Student', lastName: parts[2] || `${requests.length + 1}`, password: 'Welcome123!', roles: ['ROLE_USER'], organizationId: currentUser?.organization?.id, timezone: Intl.DateTimeFormat().resolvedOptions().timeZone });
                 }
             }
         } else {
             // Just one email on this line
             requests.push({ email: line, firstName: 'Student', lastName: `${requests.length + 1}`, password: 'Welcome123!', roles: ['ROLE_USER'], organizationId: currentUser?.organization?.id, timezone: Intl.DateTimeFormat().resolvedOptions().timeZone });
         }
      });

      if(requests.length === 0) throw new Error("No valid emails found");

      const res = await api.post('/api/v1/users/bulk', requests);
      if (res.data?.success) {
        showToast(res.data.message || 'Users bulk imported successfully!');
        setShowBulkModal(false);
        setBulkInput('');
        fetchTeam();
      } else {
        showToast('Partial failure during bulk import', 'error');
      }
    } catch (err) {
      showToast(err.response?.data?.message || err.message || 'An error occurred during bulk import', 'error');
    } finally {
      setInviting(false);
    }
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target.result;
      // Strip out the CSV header if it looks like one, or just append
      // In this case, just appending it and letting the backend or split logic handle it is fine
      setBulkInput(prev => prev ? prev + '\\n' + content : content);
    };
    reader.readAsText(file);
    e.target.value = null; // reset so same file can be uploaded again if needed
  };

  const filtered = users.filter(u =>
    `${u.firstName} ${u.lastName} ${u.email}`.toLowerCase().includes(search.toLowerCase())
  );

  const getRoleLabel = (user) => {
    if (!user.roles || user.roles.length === 0) return 'MEMBER';
    const r = user.roles[0];
    return (r.name || r).replace('ROLE_','');
  };

  const avatarBg = (name) => {
    const colors = ['#4f8ef7','#9b72f7','#10d9a0','#f7a94f','#f74f6e'];
    return colors[(name || '').charCodeAt(0) % colors.length];
  };

  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem', margin: 0, marginBottom: '0.25rem' }}>Team Members</h2>
          <p style={{ margin: 0, fontSize: '0.875rem' }}>{users.length} member{users.length !== 1 ? 's' : ''} in your organization</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            <Search size={15} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              className="input-field"
              placeholder="Search members..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              style={{ paddingLeft: '2.25rem', width: '220px', fontSize: '0.875rem', padding: '0.55rem 0.9rem 0.55rem 2.25rem' }}
            />
          </div>
          <button className="glass-button" style={{ marginRight: '0.5rem', background: 'rgba(255,255,255,0.05)' }} onClick={() => setShowBulkModal(true)}>
            <Users size={15} /> Bulk Import
          </button>
          <button className="glass-button primary" onClick={() => setShowInviteModal(true)}>
            <Plus size={15} /> Invite Member
          </button>
        </div>
      </div>

      {error && (
        <div style={{ color: 'var(--brand-danger)', marginBottom: '1rem', padding: '0.75rem 1rem', background: 'rgba(247,79,110,0.1)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(247,79,110,0.2)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>{error}</div>
          {error.includes('refresh') && (
            <button onClick={logout} className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}>
              Refresh & Log In
            </button>
          )}
        </div>
      )}

      {loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {[1,2,3].map(i => <div key={i} className="skeleton" style={{ height: '80px', borderRadius: 'var(--radius-md)' }} />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="glass-card empty-state">
          <div className="empty-icon"><Users size={32} color="var(--text-muted)" /></div>
          <h3>{search ? 'No members found' : 'No Team Members'}</h3>
          <p>{search ? 'Try a different search term.' : 'Start by inviting members to your organization.'}</p>
          {!search && <button className="btn btn-primary" onClick={() => setShowInviteModal(true)}><Plus size={16}/> Invite First Member</button>}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {filtered.map(user => {
            const role = getRoleLabel(user);
            const roleStyle = ROLE_COLORS[role] || ROLE_COLORS.MEMBER;
            const initials = `${user.firstName?.[0]||''}${user.lastName?.[0]||''}`.toUpperCase() || 'U';
            return (
              <div key={user.id} className="glass-card" style={{ padding: '1.25rem', display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
                <div className="avatar" style={{ background: avatarBg(user.firstName), width: '48px', height: '48px', fontSize: '1.1rem', boxShadow: `0 0 12px rgba(0,0,0,0.3)` }}>
                  {initials}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontWeight: 700, fontSize: '1rem', marginBottom: '0.3rem' }}>{user.firstName} {user.lastName}</div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}><Mail size={13}/> {user.email}</span>
                    {user.phoneNumber && <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}><Phone size={13}/> {user.phoneNumber}</span>}
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}><Calendar size={13}/> Joined {new Date(user.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem', flexShrink: 0 }}>
                  <span style={{ padding: '0.25rem 0.75rem', borderRadius: '2rem', fontSize: '0.72rem', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', background: roleStyle.bg, color: roleStyle.color }}>
                    {role}
                  </span>
                  {user.active !== false && (
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.72rem', color: 'var(--brand-accent)' }}>
                      <CheckCircle size={11}/> Active
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {toast && (
        <div className="toast-container">
          <div className={`toast toast-${toast.type}`}>{toast.msg}</div>
        </div>
      )}
      {/* Invite Modal */}
      {showInviteModal && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '450px', padding: '2rem', margin: '1rem', position: 'relative' }}>
            <button onClick={() => setShowInviteModal(false)} style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={20} /></button>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem', fontWeight: 600 }}>Invite Team Member</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>Send an invitation to join your organization.</p>
            
            <form onSubmit={handleInviteSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div className="input-group">
                  <label className="input-label">First Name</label>
                  <input type="text" name="firstName" required className="input-field" value={inviteData.firstName} onChange={handleInviteChange} />
                </div>
                <div className="input-group">
                  <label className="input-label">Last Name</label>
                  <input type="text" name="lastName" required className="input-field" value={inviteData.lastName} onChange={handleInviteChange} />
                </div>
              </div>
              
              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label">Email Address</label>
                <input type="email" name="email" required className="input-field" value={inviteData.email} onChange={handleInviteChange} />
              </div>

              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label">Initial Password (Temporary)</label>
                <input type="password" name="password" required className="input-field" value={inviteData.password} onChange={handleInviteChange} placeholder="Must be at least 8 characters" minLength="8" />
              </div>
              
              <div className="input-group" style={{ marginBottom: '2rem' }}>
                <label className="input-label">Role Definition</label>
                <select name="role" className="input-field" value={inviteData.role} onChange={handleInviteChange}>
                  <option value="ROLE_USER">Standard User</option>
                  <option value="ROLE_MANAGER">Manager (Creates schedules)</option>
                  <option value="ROLE_ADMIN">Administrator (Full Access)</option>
                </select>
              </div>
              
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowInviteModal(false)} disabled={inviting}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={inviting} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {inviting ? <div className="spinner" style={{width: 16, height: 16}} /> : <Mail size={16} />}
                  Send Invitation
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Bulk Import Modal */}
      {showBulkModal && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '600px', padding: '2.5rem', margin: '1rem', position: 'relative' }}>
            <button onClick={() => setShowBulkModal(false)} style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={20} /></button>
            <h3 style={{ fontSize: '1.35rem', marginBottom: '0.5rem', fontWeight: 600, fontFamily: 'Outfit' }}>Bulk Import Students</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
              Paste a list of emails (separated by commas or new lines), or paste CSV data in the format:<br/> <code>Email, FirstName, LastName</code>
            </p>
            
            <form onSubmit={handleBulkSubmit}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Or automatically load from file:</span>
                <label className="glass-button btn-sm" style={{ cursor: 'pointer', fontSize: '0.8rem', padding: '0.4rem 0.8rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Upload size={14} /> Upload CSV
                  <input type="file" accept=".csv" onChange={handleFileUpload} style={{ display: 'none' }} />
                </label>
              </div>
              <div className="input-group" style={{ marginBottom: '1.5rem' }}>
                <textarea 
                  className="input-field" 
                  rows="8" 
                  placeholder="student1@college.edu, Jane, Doe&#10;student2@college.edu, John, Smith&#10;student3@college.edu"
                  value={bulkInput}
                  onChange={e => setBulkInput(e.target.value)}
                  style={{ fontFamily: 'monospace', resize: 'vertical' }}
                  required
                />
              </div>
              
              <div style={{ padding: '1rem', background: 'rgba(6, 182, 212, 0.1)', border: '1px solid rgba(6, 182, 212, 0.3)', borderRadius: '0.5rem', marginBottom: '2rem', fontSize: '0.85rem', color: 'var(--text-primary)' }}>
                <strong>Note:</strong> All imported users will be assigned the <code>ROLE_USER</code> (Student) role and given a default password <code>Welcome123!</code> which they should change upon next login.
              </div>
              
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowBulkModal(false)} disabled={inviting}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={inviting} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {inviting ? <div className="spinner" style={{width: 16, height: 16}} /> : <Users size={16} />}
                  Import Users
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TeamMembers;
