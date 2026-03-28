import React from 'react';
import api from '../../services/api';
import { BarChart3, Users, Calendar, Clock, Layers, TrendingUp, Activity, Server, ArrowUpRight, ArrowDownRight } from 'lucide-react';

const StatCard = ({ icon: Icon, label, value, change, changeType, color, gradient }) => (
  <div style={{
    background: 'var(--bg-secondary)', border: '1px solid var(--border-color)',
    borderRadius: 'var(--radius-lg)', padding: '1.5rem', position: 'relative',
    overflow: 'hidden', transition: 'transform 0.2s, box-shadow 0.2s',
  }}
    onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.3)'; }}
    onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none'; }}
  >
    <div style={{ position: 'absolute', top: '-20px', right: '-20px', width: '80px', height: '80px', borderRadius: '50%', background: gradient, opacity: 0.08 }} />
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
      <div style={{ background: `${color}18`, padding: '0.5rem', borderRadius: 'var(--radius-sm)' }}>
        <Icon size={18} color={color} />
      </div>
      {change !== undefined && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem', fontWeight: 600, color: changeType === 'up' ? '#10d9a0' : '#f74f6e' }}>
          {changeType === 'up' ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
          {change}%
        </div>
      )}
    </div>
    <div style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.25rem', letterSpacing: '-0.02em' }}>{value}</div>
    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{label}</div>
  </div>
);

const MiniBarChart = ({ data, color }) => {
  const max = Math.max(...data, 1);
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: '3px', height: '60px' }}>
      {data.map((val, i) => (
        <div key={i} style={{
          flex: 1, minWidth: '8px', borderRadius: '3px 3px 0 0',
          background: i === data.length - 1 ? color : `${color}60`,
          height: `${(val / max) * 100}%`, transition: 'height 0.3s ease',
        }} />
      ))}
    </div>
  );
};

const Reports = () => {
  const [stats, setStats] = React.useState({
    totalUsers: 0, totalDepartments: 0, totalTimetables: 0,
    totalResources: 0, totalEvents: 0, recentActivities: 0,
  });
  const [loading, setLoading] = React.useState(true);
  const [weeklyData] = React.useState([3, 7, 5, 12, 8, 15, 10]);
  const [resourceData] = React.useState([65, 72, 58, 80, 45, 90, 75]);

  React.useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    setLoading(true);
    try {
      const [usersRes, deptsRes, timetablesRes, resourcesRes] = await Promise.allSettled([
        api.get('/users?size=1'),
        api.get('/departments'),
        api.get('/timetables?size=1'),
        api.get('/resources'),
      ]);

      setStats({
        totalUsers: usersRes.status === 'fulfilled' ? (usersRes.value.data.data?.totalElements || usersRes.value.data.data?.length || 0) : 0,
        totalDepartments: deptsRes.status === 'fulfilled' ? (deptsRes.value.data.data?.length || 0) : 0,
        totalTimetables: timetablesRes.status === 'fulfilled' ? (timetablesRes.value.data.data?.totalElements || timetablesRes.value.data.data?.length || 0) : 0,
        totalResources: resourcesRes.status === 'fulfilled' ? (resourcesRes.value.data.data?.length || 0) : 0,
        totalEvents: 0,
        recentActivities: 0,
      });
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  return (
    <div style={{ padding: '2rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
          <div style={{ background: 'linear-gradient(135deg, #f59e0b, #ef4444)', padding: '0.6rem', borderRadius: 'var(--radius-md)', boxShadow: '0 4px 14px rgba(245,158,11,0.35)' }}>
            <BarChart3 color="white" size={22} />
          </div>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 700 }}>Reports & Analytics</h1>
            <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              Organization overview and usage insights
            </p>
          </div>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          <div className="spinner" style={{ margin: '0 auto 1rem' }}></div>
          Loading analytics...
        </div>
      ) : (
        <>
          {/* Stat Cards Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
            <StatCard icon={Users} label="Total Members" value={stats.totalUsers} change={12} changeType="up" color="#4f8ef7" gradient="linear-gradient(135deg, #4f8ef7, #6366f1)" />
            <StatCard icon={Layers} label="Departments" value={stats.totalDepartments} color="#a855f7" gradient="linear-gradient(135deg, #a855f7, #ec4899)" />
            <StatCard icon={Clock} label="Timetables" value={stats.totalTimetables} change={8} changeType="up" color="#10d9a0" gradient="linear-gradient(135deg, #10d9a0, #06b6d4)" />
            <StatCard icon={Server} label="Resources" value={stats.totalResources} color="#f59e0b" gradient="linear-gradient(135deg, #f59e0b, #ef4444)" />
          </div>

          {/* Charts Row */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Weekly Activity */}
            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-lg)', padding: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>Weekly Activity</h3>
                  <p style={{ margin: '0.25rem 0 0', fontSize: '0.75rem', color: 'var(--text-muted)' }}>Schedules created this week</p>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', padding: '0.25rem 0.6rem', borderRadius: '2rem', background: 'rgba(16,217,160,0.12)', fontSize: '0.72rem', color: '#10d9a0', fontWeight: 600 }}>
                  <TrendingUp size={12} /> +23%
                </div>
              </div>
              <MiniBarChart data={weeklyData} color="#4f8ef7" />
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem' }}>
                {days.map(d => <span key={d} style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>{d}</span>)}
              </div>
            </div>

            {/* Resource Utilization */}
            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-lg)', padding: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>Resource Utilization</h3>
                  <p style={{ margin: '0.25rem 0 0', fontSize: '0.75rem', color: 'var(--text-muted)' }}>Room & equipment usage this week</p>
                </div>
                <div style={{ padding: '0.25rem 0.6rem', borderRadius: '2rem', background: 'rgba(79,142,247,0.12)', fontSize: '0.72rem', color: '#4f8ef7', fontWeight: 600 }}>
                  Avg: 69%
                </div>
              </div>
              <MiniBarChart data={resourceData} color="#10d9a0" />
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem' }}>
                {days.map(d => <span key={d} style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>{d}</span>)}
              </div>
            </div>
          </div>

          {/* Quick Insights */}
          <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-lg)', padding: '1.5rem' }}>
            <h3 style={{ margin: '0 0 1rem', fontSize: '1rem', fontWeight: 600 }}>Quick Insights</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem' }}>
              {[
                { icon: Activity, label: 'Most Active Day', value: 'Wednesday', detail: '15 schedules created', color: '#4f8ef7' },
                { icon: Users, label: 'Most Active User', value: 'Admin', detail: 'Leading in contributions', color: '#a855f7' },
                { icon: Server, label: 'Most Used Resource', value: 'Room A-101', detail: '90% utilization rate', color: '#10d9a0' },
                { icon: Calendar, label: 'Upcoming Events', value: '3 this week', detail: 'Next: Team Meeting', color: '#f59e0b' },
              ].map((item, idx) => (
                <div key={idx} style={{
                  display: 'flex', alignItems: 'center', gap: '1rem',
                  padding: '1rem', borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-color)', transition: 'background 0.2s'
                }}
                  onMouseEnter={e => e.currentTarget.style.background = 'rgba(79,142,247,0.03)'}
                  onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                >
                  <div style={{ background: `${item.color}18`, padding: '0.6rem', borderRadius: 'var(--radius-sm)' }}>
                    <item.icon size={18} color={item.color} />
                  </div>
                  <div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.15rem' }}>{item.label}</div>
                    <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{item.value}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{item.detail}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Reports;
