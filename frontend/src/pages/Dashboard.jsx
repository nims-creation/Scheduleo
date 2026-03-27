import React from 'react';
import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Calendar, Clock, Users, LayoutDashboard, Zap, Layers, Server, Bell, Check, CreditCard } from 'lucide-react';
import { useNavigate, Routes, Route, Link, useLocation } from 'react-router-dom';
import api from '../services/api';
import DashboardHome from './dashboard/DashboardHome';
import TeamMembers from './dashboard/TeamMembers';
import Timetables from './dashboard/Timetables';
import OrganizationSettings from './dashboard/OrganizationSettings';
import Departments from './dashboard/Departments';
import CalendarPage from './dashboard/Calendar';
import Resources from './dashboard/Resources';
import Billing from './dashboard/Billing';
import ProfileSettings from './dashboard/ProfileSettings';

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
  const [notifications, setNotifications] = React.useState([]);
  const [unreadCount, setUnreadCount] = React.useState(0);
  const [showNotifications, setShowNotifications] = React.useState(false);

  React.useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 60000); // Check every minute
    return () => clearInterval(interval);
  }, []);

  const fetchNotifications = async () => {
    try {
      const { data } = await api.get('/notifications');
      setNotifications(data.data || []);
      setUnreadCount(data.data?.filter(n => !n.isRead).length || 0);
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await api.put(`/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await api.put('/notifications/read-all');
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

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
          <NavItem to="/dashboard/calendar" icon={Calendar} label="Calendar" />
          <NavItem to="/dashboard/timetables" icon={Clock} label="Timetables" />

          <div style={{ fontSize: '0.65rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.1em', padding: '1rem 0.5rem 0.25rem' }}>Organization</div>
          <NavItem to="/dashboard/team" icon={Users} label="Team Members" />
          <NavItem to="/dashboard/departments" icon={Layers} label="Departments" />
          <NavItem to="/dashboard/resources" icon={Server} label="Resources" />
          <NavItem to="/dashboard/billing" icon={CreditCard} label="Billing" />
          <NavItem to="/dashboard/settings" icon={Zap} label="Settings" />
        </nav>

        {/* User Profile Bubble */}
        <div style={{ padding: '1rem 0.75rem', borderTop: '1px solid var(--border-color)' }}>
          <Link to="/dashboard/profile" style={{ textDecoration: 'none' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', background: 'transparent', padding: '0.5rem', borderRadius: 'var(--radius-md)', cursor: 'pointer', transition: 'background 0.2s', border: '1px solid transparent' }}
                 onMouseEnter={e => { e.currentTarget.style.background = 'var(--bg-secondary)'; e.currentTarget.style.borderColor = 'var(--border-color)'; }}
                 onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.borderColor = 'transparent'; }}
            >
              <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--brand-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: '0.875rem' }}>
                {user?.firstName?.charAt(0) || ''}{user?.lastName?.charAt(0) || ''}
              </div>
              <div style={{ flex: 1, overflow: 'hidden' }}>
                <div style={{ fontWeight: 600, fontSize: '0.875rem', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden', color: 'var(--text-primary)' }}>
                  {user?.firstName} {user?.lastName}
                </div>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden', textTransform: 'capitalize' }}>
                  {user?.roles?.[0]?.replace('ROLE_', '').toLowerCase()}
                </div>
              </div>
            </div>
          </Link>
        </div>

        {/* Logout */}
        <div style={{ padding: '0.5rem 0.75rem 1rem' }}>
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
            <div style={{ position: 'relative' }}>
              <button onClick={() => setShowNotifications(!showNotifications)} style={{ background: 'transparent', border: '1px solid var(--border-color)', borderRadius: '50%', width: '36px', height: '36px', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: 'var(--text-primary)', position: 'relative' }}>
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span style={{ position: 'absolute', top: '-2px', right: '-2px', background: 'var(--brand-danger)', color: 'white', fontSize: '0.65rem', fontWeight: 700, width: '16px', height: '16px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    {unreadCount}
                  </span>
                )}
              </button>
              
              {showNotifications && (
                <div style={{ position: 'absolute', top: '45px', right: 0, width: '320px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', boxShadow: '0 10px 25px rgba(0,0,0,0.5)', zIndex: 1000, overflow: 'hidden' }}>
                  <div style={{ padding: '1rem', borderBottom: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3 style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>Notifications</h3>
                    {unreadCount > 0 && (
                      <button onClick={handleMarkAllAsRead} style={{ background: 'none', border: 'none', color: 'var(--brand-primary)', fontSize: '0.75rem', cursor: 'pointer', fontWeight: 600 }}>Mark all read</button>
                    )}
                  </div>
                  <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    {notifications.length === 0 ? (
                      <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.875rem' }}>No notifications yet</div>
                    ) : (
                      notifications.map(notification => (
                        <div key={notification.id} style={{ padding: '1rem', borderBottom: '1px solid var(--border-color)', background: notification.isRead ? 'transparent' : 'rgba(79, 142, 247, 0.05)', display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                          <div style={{ flex: 1 }}>
                            <div style={{ fontWeight: 600, fontSize: '0.875rem', color: notification.isRead ? 'var(--text-primary)' : 'var(--brand-primary)', marginBottom: '0.25rem' }}>{notification.title}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{notification.message}</div>
                            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>{new Date(notification.createdAt).toLocaleString()}</div>
                          </div>
                          {!notification.isRead && (
                            <button onClick={() => handleMarkAsRead(notification.id)} style={{ background: 'none', border: 'none', color: 'var(--brand-success)', cursor: 'pointer', padding: '0.25rem' }} title="Mark as read">
                              <Check size={16} />
                            </button>
                          )}
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
            
            <div style={{ padding: '0.3rem 0.75rem', background: 'rgba(16,217,160,0.12)', border: '1px solid rgba(16,217,160,0.25)', borderRadius: '2rem', fontSize: '0.72rem', color: 'var(--brand-accent)', fontWeight: 600 }}>
              ● LIVE
            </div>
          </div>
        </header>

        {/* Routes */}
        <div style={{ flex: 1, overflow: 'auto' }}>
          <Routes>
            <Route path="/" element={<DashboardHome />} />
            <Route path="calendar" element={<CalendarPage />} />
            <Route path="team" element={<TeamMembers />} />
            <Route path="timetables" element={<Timetables />} />
            <Route path="departments" element={<Departments />} />
            <Route path="resources" element={<Resources />} />
            <Route path="billing" element={<Billing />} />
            <Route path="settings" element={<OrganizationSettings />} />
            <Route path="profile" element={<ProfileSettings />} />
          </Routes>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
