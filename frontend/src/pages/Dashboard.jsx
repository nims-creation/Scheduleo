import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Calendar, Clock, Users, Building, Settings } from 'lucide-react';
import { useNavigate, Routes, Route, Link, useLocation } from 'react-router-dom';

import DashboardHome from './dashboard/DashboardHome';
import TeamMembers from './dashboard/TeamMembers';
import Timetables from './dashboard/Timetables';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  const getLinkStyle = (path) => {
    const isActive = location.pathname === path || (path !== '/dashboard' && location.pathname.startsWith(path));
    return {
      display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', 
      borderRadius: 'var(--radius-sm)', textDecoration: 'none',
      color: isActive ? 'white' : 'var(--text-secondary)', 
      backgroundColor: isActive ? 'var(--bg-glass-hover)' : 'transparent'
    };
  };

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
          <Link to="/dashboard" style={getLinkStyle('/dashboard')}>
            <Calendar size={18} /> Dashboard
          </Link>
          <Link to="/dashboard/timetables" style={getLinkStyle('/dashboard/timetables')}>
            <Clock size={18} /> Timetables
          </Link>
          
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: '600', marginTop: '1.5rem', marginBottom: '0.5rem' }}>Organization</div>
          <Link to="/dashboard/team" style={getLinkStyle('/dashboard/team')}>
            <Users size={18} /> Team Members
          </Link>
          <Link to="#" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', color: 'var(--text-secondary)', textDecoration: 'none' }}>
            <Building size={18} /> Departments
          </Link>
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
        <Routes>
          <Route path="/" element={<DashboardHome />} />
          <Route path="team" element={<TeamMembers />} />
          <Route path="timetables" element={<Timetables />} />
        </Routes>
      </main>
    </div>
  );
};

export default Dashboard;
