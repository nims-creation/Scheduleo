import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Calendar, Clock, Plus } from 'lucide-react';

const Timetables = () => {
  const [timetables, setTimetables] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTimetables = async () => {
      try {
        const response = await api.get('/api/v1/timetables');
        if (response.data && response.data.data) {
          const list = response.data.data.content || response.data.data || [];
          setTimetables(list);
        }
      } catch (err) {
        console.error('Error fetching timetables:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchTimetables();
  }, []);

  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.5rem', margin: 0 }}>Timetables</h2>
        <button className="glass-button primary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Plus size={18} /> New Timetable
        </button>
      </div>

      {loading ? (
        <div style={{ color: 'var(--text-secondary)' }}>Loading timetables...</div>
      ) : timetables.length === 0 ? (
        <div className="glass-card" style={{ textAlign: 'center', padding: '4rem', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div style={{ width: '80px', height: '80px', borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.05)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1.5rem' }}>
            <Calendar size={40} color="var(--brand-primary)" />
          </div>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>No Timetables Generated</h3>
          <p style={{ color: 'var(--text-secondary)', maxWidth: '400px', marginBottom: '2rem' }}>Create your first active schedule or pattern matrix so your staff can be accurately scheduled.</p>
          <button className="glass-button primary">Generate Matrix</button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
          {timetables.map(tt => (
            <div key={tt.id} className="glass-card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem', marginBottom: '1rem' }}>
                <h3 style={{ fontSize: '1.1rem', margin: 0 }}>{tt.name || 'Master Timetable'}</h3>
                <span style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', borderRadius: '4px', backgroundColor: tt.isActive ? 'rgba(34, 197, 94, 0.2)' : 'rgba(200, 200, 200, 0.1)', color: tt.isActive ? '#4ade80' : 'var(--text-secondary)' }}>
                  {tt.isActive ? 'ACTIVE' : 'DRAFT'}
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', flex: 1 }}>{tt.description || 'Standard weekly rotation schedule pattern.'}</p>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Clock size={14} /> Created {new Date(tt.createdAt).toLocaleDateString()}</span>
                <button style={{ background: 'transparent', border: 'none', color: 'var(--brand-primary)', cursor: 'pointer', fontWeight: '500' }}>View Details &rarr;</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Timetables;
