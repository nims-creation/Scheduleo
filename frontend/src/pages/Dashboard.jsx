import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Calendar, Clock, Users, Building, Settings } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-primary)' }}>
      {/* Sidebar */}
      <aside style={{ width: 'var(--sidebar-width)', backgroundColor: 'var(--bg-secondary)', borderRight: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column' }}>
        <div style={{ padding: '1.5rem', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ background: 'var(--brand-gradient)', padding: '0.4rem', borderRadius: 'var(--radius-sm)' }}>
            <Calendar color="white" size={20} />
          </div>
          <span style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>Schedulo</span>
        </div>

        <div style={{ padding: '1.5rem', flex: 1, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: '600', marginBottom: '0.5rem' }}>Overview</div>
          <a href="#" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', color: 'white', backgroundColor: 'var(--bg-glass-hover)', textDecoration: 'none' }}>
            <Calendar size={18} /> Schedule
          </a>
          <a href="#" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', color: 'var(--text-secondary)', textDecoration: 'none' }}>
            <Clock size={18} /> Timetables
          </a>
          
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: '600', marginTop: '1.5rem', marginBottom: '0.5rem' }}>Organization</div>
          <a href="#" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', color: 'var(--text-secondary)', textDecoration: 'none' }}>
            <Users size={18} /> Team Members
          </a>
          <a href="#" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', color: 'var(--text-secondary)', textDecoration: 'none' }}>
            <Building size={18} /> Departments
          </a>
        </div>

        <div style={{ padding: '1.5rem', borderTop: '1px solid var(--border-color)' }}>
          <button onClick={handleLogout} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#ef4444', backgroundColor: 'transparent', border: 'none', cursor: 'pointer', fontSize: '1rem', width: '100%' }}>
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {/* Header */}
        <header style={{ height: 'var(--header-height)', backgroundColor: 'var(--bg-glass)', backdropFilter: 'blur(12px)', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2rem' }}>
          <h2 style={{ fontSize: '1.25rem', margin: 0 }}>My Schedule</h2>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontWeight: '500', fontSize: '0.875rem' }}>{user?.firstName} {user?.lastName}</div>
              <div style={{ color: 'var(--brand-primary)', fontSize: '0.75rem' }}>{user?.roles?.[0] || 'User'}</div>
            </div>
            <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--brand-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
              {user?.firstName?.charAt(0) || 'U'}
            </div>
          </div>
        </header>

        {/* Dynamic Content */}
        <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
          <div className="glass-card animate-slide-up" style={{ backgroundColor: 'rgba(59, 130, 246, 0.1)', borderColor: 'rgba(59, 130, 246, 0.2)' }}>
            <h3 style={{ color: 'white' }}>Welcome to Schedulo, {user?.firstName}!</h3>
            <p style={{ marginTop: '0.5rem' }}>Your dashboard is ready. This authenticated environment uses JWT tokens to securely fetch your organizational data from the Spring Boot backend.</p>
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
      </main>
    </div>
  );
};

export default Dashboard;
