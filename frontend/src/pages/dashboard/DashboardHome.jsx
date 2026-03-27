import React from 'react';
import { useAuth } from '../../context/AuthContext';

const DashboardHome = () => {
  const { user } = useAuth();
  
  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
      <div className="glass-card animate-slide-up" style={{ backgroundColor: 'rgba(59, 130, 246, 0.1)', borderColor: 'rgba(59, 130, 246, 0.2)' }}>
        <h3 style={{ color: 'white' }}>Welcome to Schedulo, {user?.firstName}!</h3>
        <p style={{ marginTop: '0.5rem' }}>Your dashboard is ready. Select a module from the sidebar to manage your organization's resources.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginTop: '2rem' }}>
         <div className="glass-card">
           <h4 style={{ color: 'var(--text-secondary)' }}>Upcoming Events</h4>
           <div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0.5rem 0' }}>0</div>
           <p style={{ fontSize: '0.875rem' }}>No conflicts detected</p>
         </div>
         <div className="glass-card">
           <h4 style={{ color: 'var(--text-secondary)' }}>Active Timetables</h4>
           <div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0.5rem 0' }}>0</div>
           <p style={{ fontSize: '0.875rem' }}>Create your first pattern</p>
         </div>
         <div className="glass-card">
           <h4 style={{ color: 'var(--text-secondary)' }}>Pending Requests</h4>
           <div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0.5rem 0' }}>0</div>
           <p style={{ fontSize: '0.875rem' }}>All caught up!</p>
         </div>
      </div>
    </div>
  );
};

export default DashboardHome;
