import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Users, Mail, Phone, Calendar } from 'lucide-react';

const TeamMembers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await api.get('/api/v1/users');
        if (response.data && response.data.data) {
          // Check if response has content property (Pageable) or is just an array
          const usersList = response.data.data.content || response.data.data || [];
          setUsers(usersList);
        }
      } catch (err) {
        console.error('Error fetching users:', err);
        setError('Failed to load team members. Are you in a valid organization?');
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.5rem', margin: 0 }}>Team Members</h2>
        <button className="glass-button primary">Add Member</button>
      </div>

      {error && <div style={{ color: '#ef4444', marginBottom: '1rem' }}>{error}</div>}

      {loading ? (
        <div style={{ color: 'var(--text-secondary)' }}>Loading team members...</div>
      ) : users.length === 0 ? (
        <div className="glass-card" style={{ textAlign: 'center', padding: '3rem' }}>
          <Users size={48} color="var(--text-muted)" style={{ margin: '0 auto 1rem' }} />
          <p style={{ color: 'var(--text-secondary)' }}>No team members found. Start by inviting some!</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {users.map(user => (
            <div key={user.id} className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.25rem' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: '50%', backgroundColor: 'var(--brand-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '1.25rem' }}>
                {user.firstName?.charAt(0) || 'U'}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: '600', fontSize: '1.1rem' }}>{user.firstName} {user.lastName}</div>
                <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}><Mail size={14} /> {user.email}</span>
                  {user.phoneNumber && <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}><Phone size={14} /> {user.phoneNumber}</span>}
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}><Calendar size={14} /> Joined {new Date(user.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
              <div>
                <span style={{ padding: '0.25rem 0.75rem', backgroundColor: 'rgba(59, 130, 246, 0.2)', color: 'var(--brand-primary)', borderRadius: '1rem', fontSize: '0.75rem', fontWeight: 'bold' }}>
                  {user.roles && user.roles.length > 0 ? user.roles[0]?.name?.replace('ROLE_', '') : 'MEMBER'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TeamMembers;
