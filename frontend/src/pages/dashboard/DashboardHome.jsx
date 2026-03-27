import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { Calendar, Users, Zap, ArrowRight, CheckCircle, AlertCircle, TrendingUp, BookOpen } from 'lucide-react';

const DashboardHome = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({ timetables: 0, members: 0, activeTimetables: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [ttRes, usersRes] = await Promise.allSettled([
          api.get('/api/v1/timetables'),
          api.get('/api/v1/users'),
        ]);
        const ttData = ttRes.status === 'fulfilled' ? ttRes.value.data?.data : null;
        const usersData = usersRes.status === 'fulfilled' ? usersRes.value.data?.data : null;
        const timetables = ttData?.content || ttData || [];
        const members = usersData?.content || usersData || [];
        const active = timetables.filter(t => t.status === 'PUBLISHED' || t.isActive).length;
        setStats({ timetables: timetables.length, members: Array.isArray(members) ? members.length : 0, activeTimetables: active });
      } catch (e) { /* silent */ }
      finally { setLoading(false); }
    };
    fetchStats();
  }, []);

  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

  const statCards = [
    { label: 'Total Timetables', value: loading ? '...' : stats.timetables, icon: Calendar, color: 'var(--brand-primary)', bg: 'rgba(79,142,247,0.1)', hint: 'All timetable records' },
    { label: 'Active Schedules', value: loading ? '...' : stats.activeTimetables, icon: CheckCircle, color: 'var(--brand-accent)', bg: 'rgba(16,217,160,0.1)', hint: 'Published & live now' },
    { label: 'Team Members', value: loading ? '...' : stats.members, icon: Users, color: 'var(--brand-secondary)', bg: 'rgba(155,114,247,0.1)', hint: 'In your organization' },
    { label: 'Pending Conflicts', value: '0', icon: AlertCircle, color: 'var(--brand-warning)', bg: 'rgba(247,169,79,0.1)', hint: 'All clear!' },
  ];

  const quickActions = [
    { label: 'View Timetables', desc: 'Browse and manage schedules', icon: Calendar, path: '/dashboard/timetables', rgb: '79,142,247', color: 'var(--brand-primary)' },
    { label: 'Manage Team', desc: 'View and invite members', icon: Users, path: '/dashboard/team', rgb: '155,114,247', color: 'var(--brand-secondary)' },
    { label: 'Generate Schedule', desc: 'Auto-create with AI engine', icon: Zap, path: '/dashboard/timetables', rgb: '16,217,160', color: 'var(--brand-accent)' },
  ];

  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
      <div className="animate-slide-up" style={{
        background: 'linear-gradient(135deg, rgba(79,142,247,0.15) 0%, rgba(155,114,247,0.12) 100%)',
        border: '1px solid rgba(79,142,247,0.2)', borderRadius: 'var(--radius-lg)',
        padding: '2rem', marginBottom: '2rem', position: 'relative', overflow: 'hidden'
      }}>
        <div style={{ position: 'absolute', top: '-30px', right: '-30px', width: '150px', height: '150px', background: 'radial-gradient(circle, rgba(79,142,247,0.2), transparent 70%)', borderRadius: '50%' }} />
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--brand-primary)', fontWeight: 600, marginBottom: '0.4rem', textTransform: 'uppercase', letterSpacing: '0.08em' }}>👋 {greeting}</div>
            <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem', color: 'white' }}>{user?.firstName} {user?.lastName}</h2>
            <p style={{ color: 'var(--text-secondary)', margin: 0 }}>Welcome back to Schedulo — your intelligent scheduling platform.</p>
          </div>
          <button className="btn btn-primary" onClick={() => navigate('/dashboard/timetables')}>
            <Zap size={16} /> Generate Timetable <ArrowRight size={16} />
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
        {statCards.map((sc) => (
          <div key={sc.label} className="stat-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div>
                <div className="stat-label">{sc.label}</div>
                <div className="stat-value" style={{ color: sc.color }}>{sc.value}</div>
              </div>
              <div style={{ width: '44px', height: '44px', borderRadius: 'var(--radius-sm)', background: sc.bg, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <sc.icon size={22} color={sc.color} />
              </div>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>{sc.hint}</p>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
        <div className="glass-card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
            <TrendingUp size={18} color="var(--brand-primary)" />
            <h3 style={{ fontSize: '1rem', margin: 0 }}>Quick Actions</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {quickActions.map(qa => (
              <button key={qa.label} onClick={() => navigate(qa.path)} style={{
                display: 'flex', alignItems: 'center', gap: '1rem', padding: '0.9rem 1rem',
                background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-sm)', cursor: 'pointer', transition: 'all 0.2s', textAlign: 'left', width: '100%'
              }}
                onMouseEnter={e => { e.currentTarget.style.background = `rgba(${qa.rgb},0.08)`; e.currentTarget.style.borderColor = qa.color; }}
                onMouseLeave={e => { e.currentTarget.style.background = 'rgba(255,255,255,0.03)'; e.currentTarget.style.borderColor = 'var(--border-color)'; }}
              >
                <div style={{ width: '36px', height: '36px', borderRadius: 'var(--radius-sm)', background: `rgba(${qa.rgb},0.15)`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <qa.icon size={18} color={qa.color} />
                </div>
                <div>
                  <div style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)' }}>{qa.label}</div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{qa.desc}</div>
                </div>
                <ArrowRight size={16} color="var(--text-muted)" style={{ marginLeft: 'auto' }} />
              </button>
            ))}
          </div>
        </div>

        <div className="glass-card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
            <BookOpen size={18} color="var(--brand-accent)" />
            <h3 style={{ fontSize: '1rem', margin: 0 }}>How it Works</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
            {[
              { step: '1', text: 'Create a Timetable with a date range', done: stats.timetables > 0 },
              { step: '2', text: 'Add Time Slots for each day of the week', done: false },
              { step: '3', text: 'Generate classes using the AI engine', done: false },
              { step: '4', text: 'Publish to make it live for your team', done: false },
            ].map(item => (
              <div key={item.step} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{
                  width: '28px', height: '28px', borderRadius: '50%', flexShrink: 0,
                  background: item.done ? 'rgba(16,217,160,0.15)' : 'rgba(255,255,255,0.06)',
                  border: `1px solid ${item.done ? 'rgba(16,217,160,0.4)' : 'var(--border-color)'}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '0.75rem', fontWeight: 700, color: item.done ? 'var(--brand-accent)' : 'var(--text-muted)'
                }}>
                  {item.done ? <CheckCircle size={14} /> : item.step}
                </div>
                <span style={{ fontSize: '0.875rem', color: item.done ? 'var(--text-secondary)' : 'var(--text-primary)', textDecoration: item.done ? 'line-through' : 'none' }}>{item.text}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardHome;
