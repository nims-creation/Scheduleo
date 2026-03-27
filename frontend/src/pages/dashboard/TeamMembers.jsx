import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Users, Mail, Phone, Calendar, Plus, Search, Shield, X, CheckCircle } from 'lucide-react';

const ROLE_COLORS = { ADMIN: { bg: 'rgba(247,79,110,0.15)', color: 'var(--brand-danger)' }, MANAGER: { bg: 'rgba(79,142,247,0.15)', color: 'var(--brand-primary)' }, MEMBER: { bg: 'rgba(155,114,247,0.15)', color: 'var(--brand-secondary)' } };

const TeamMembers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [toast, setToast] = useState(null);

  const showToast = (msg, type='success') => { setToast({msg,type}); setTimeout(()=>setToast(null),3000); };

  useEffect(() => {
    api.get('/api/v1/users')
      .then(res => {
        const list = res.data?.data?.content || res.data?.data || [];
        setUsers(Array.isArray(list) ? list : []);
      })
      .catch(() => setError('Failed to load team members.'))
      .finally(() => setLoading(false));
  }, []);

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
          <button className="glass-button primary" onClick={() => showToast('Invite feature coming soon!', 'info')}>
            <Plus size={15} /> Invite Member
          </button>
        </div>
      </div>

      {error && <div style={{ color: 'var(--brand-danger)', marginBottom: '1rem', padding: '0.75rem 1rem', background: 'rgba(247,79,110,0.1)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(247,79,110,0.2)' }}>{error}</div>}

      {loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {[1,2,3].map(i => <div key={i} className="skeleton" style={{ height: '80px', borderRadius: 'var(--radius-md)' }} />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="glass-card empty-state">
          <div className="empty-icon"><Users size={32} color="var(--text-muted)" /></div>
          <h3>{search ? 'No members found' : 'No Team Members'}</h3>
          <p>{search ? 'Try a different search term.' : 'Start by inviting members to your organization.'}</p>
          {!search && <button className="btn btn-primary" onClick={() => showToast('Invite feature coming soon!','info')}><Plus size={16}/> Invite First Member</button>}
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
    </div>
  );
};

export default TeamMembers;
