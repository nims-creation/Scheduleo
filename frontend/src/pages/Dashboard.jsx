import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Calendar, Clock, Users, LayoutDashboard, Zap } from 'lucide-react';
import { useNavigate, Routes, Route, Link, useLocation } from 'react-router-dom';
import DashboardHome from './dashboard/DashboardHome';
import TeamMembers from './dashboard/TeamMembers';
import Timetables from './dashboard/Timetables';

const NavItem = ({ to, icon: Icon, label, exact }) => {
  const location = useLocation();
  const isActive = exact ? location.pathname === to : location.pathname.startsWith(to);
  return (
    <Link to={to} className={`nav-item ${isActive ? 'active' : ''}`}>
      <Icon size={17} /> {label}
    </Link>
  );
};

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/'); };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-primary)' }}>
      {/* Sidebar */}
      <aside style={{
        width: 'var(--sidebar-width)', backgroundColor: 'var(--bg-secondary)',
        borderRight: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column',
        position: 'sticky', top: 0, height: '100vh'
      }}>
        {/* Logo */}
        <div style={{ padding: '1.5rem 1.25rem', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ background: 'var(--brand-gradient)', padding: '0.45rem', borderRadius: 'var(--radius-sm)', boxShadow: '0 4px 12px rgba(79,142,247,0.4)' }}>
            <Calendar color="white" size={20} />
          </div>
          <div>
            <div style={{ fontSize: '1.1rem', fontWeight: 800, letterSpacing: '-0.02em' }}>Schedulo</div>
            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>v2.0</div>
          </div>
        </div>

        {/* User info */}
        <div style={{ padding: '1rem 1.25rem', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'var(--brand-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: '0.9rem', flexShrink: 0 }}>
            {user?.firstName?.charAt(0) || 'U'}
          </div>
          <div style={{ minWidth: 0 }}>
            <div style={{ fontWeight: 600, fontSize: '0.875rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.firstName} {user?.lastName}</div>
            <div style={{ color: 'var(--brand-primary)', fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{user?.roles?.[0] || 'User'}</div>
          </div>
        </div>

        {/* Navigation */}
        <nav style={{ padding: '1rem 0.75rem', flex: 1, display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
          <div style={{ fontSize: '0.65rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.1em', padding: '0.5rem 0.5rem 0.25rem' }}>Overview</div>
          <NavItem to="/dashboard" icon={LayoutDashboard} label="Dashboard" exact />
          <NavItem to="/dashboard/timetables" icon={Clock} label="Timetables" />

          <div style={{ fontSize: '0.65rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.1em', padding: '1rem 0.5rem 0.25rem' }}>Organization</div>
          <NavItem to="/dashboard/team" icon={Users} label="Team Members" />
        </nav>

        {/* Logout */}
        <div style={{ padding: '1rem 0.75rem', borderTop: '1px solid var(--border-color)' }}>
          <button onClick={handleLogout} style={{
            display: 'flex', alignItems: 'center', gap: '0.75rem',
            color: 'var(--text-muted)', backgroundColor: 'transparent', border: 'none',
            cursor: 'pointer', fontSize: '0.875rem', width: '100%', padding: '0.6rem 0.75rem',
            borderRadius: 'var(--radius-sm)', transition: 'all 0.2s'
          }}
            onMouseEnter={e => { e.currentTarget.style.color = 'var(--brand-danger)'; e.currentTarget.style.background = 'rgba(247,79,110,0.08)'; }}
            onMouseLeave={e => { e.currentTarget.style.color = 'var(--text-muted)'; e.currentTarget.style.background = 'transparent'; }}
          >
            <LogOut size={17} /> Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', minWidth: 0 }}>
        {/* Header */}
        <header style={{
          height: 'var(--header-height)', backgroundColor: 'rgba(10,12,16,0.8)',
          backdropFilter: 'blur(16px)', borderBottom: '1px solid var(--border-color)',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '0 2rem', position: 'sticky', top: 0, zIndex: 100
        }}>
          <h2 style={{ fontSize: '1.1rem', margin: 0, fontWeight: 600 }}>
            {user?.organizationName || 'My Organization'}
          </h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ padding: '0.3rem 0.75rem', background: 'rgba(16,217,160,0.12)', border: '1px solid rgba(16,217,160,0.25)', borderRadius: '2rem', fontSize: '0.72rem', color: 'var(--brand-accent)', fontWeight: 600 }}>
              ● LIVE
            </div>
          </div>
        </header>

        {/* Routes */}
        <div style={{ flex: 1, overflow: 'auto' }}>
          <Routes>
            <Route path="/" element={<DashboardHome />} />
            <Route path="team" element={<TeamMembers />} />
            <Route path="timetables" element={<Timetables />} />
          </Routes>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
