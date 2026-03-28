import React from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { Activity, User, Clock, Filter, ChevronLeft, ChevronRight, Search, Shield, Edit, Trash2, Plus, Eye, LogIn, LogOut, Download, Upload, Zap } from 'lucide-react';

const actionIcons = {
  CREATE: Plus, UPDATE: Edit, DELETE: Trash2, LOGIN: LogIn, LOGOUT: LogOut,
  VIEW: Eye, EXPORT: Download, IMPORT: Upload, GENERATE: Zap,
  ASSIGN: User, ACTIVATE: Shield, DEACTIVATE: Shield,
};

const actionColors = {
  CREATE: '#10d9a0', UPDATE: '#4f8ef7', DELETE: '#f74f6e',
  LOGIN: '#10d9a0', LOGOUT: '#f7a84f', VIEW: '#8b8fa3',
  EXPORT: '#a855f7', IMPORT: '#06b6d4', GENERATE: '#f59e0b',
  ASSIGN: '#4f8ef7', ACTIVATE: '#10d9a0', DEACTIVATE: '#f74f6e',
};

const ActivityLog = () => {
  const { user } = useAuth();
  const [logs, setLogs] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [page, setPage] = React.useState(0);
  const [totalPages, setTotalPages] = React.useState(0);
  const [totalElements, setTotalElements] = React.useState(0);
  const [searchQuery, setSearchQuery] = React.useState('');

  React.useEffect(() => {
    fetchLogs();
  }, [page]);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const { data } = await api.get(`/audit-logs?page=${page}&size=20`);
      setLogs(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
      setTotalElements(data.data?.totalElements || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filteredLogs = logs.filter(log =>
    !searchQuery ||
    log.userName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    log.action?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    log.entityType?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    log.description?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getTimeAgo = (dateStr) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div style={{ padding: '2rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ background: 'linear-gradient(135deg, #a855f7, #6366f1)', padding: '0.6rem', borderRadius: 'var(--radius-md)', boxShadow: '0 4px 14px rgba(168,85,247,0.35)' }}>
              <Activity color="white" size={22} />
            </div>
            <div>
              <h1 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 700 }}>Activity Log</h1>
              <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Track all actions across your organization
              </p>
            </div>
          </div>
          <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.5rem 1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            {totalElements} total activities
          </div>
        </div>
      </div>

      {/* Search bar */}
      <div style={{ marginBottom: '1.5rem', position: 'relative', maxWidth: '400px' }}>
        <Search size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
        <input
          type="text"
          placeholder="Search activities..."
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          style={{
            width: '100%', padding: '0.65rem 0.75rem 0.65rem 2.25rem',
            background: 'var(--bg-secondary)', border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-md)', color: 'var(--text-primary)',
            fontSize: '0.875rem', outline: 'none', boxSizing: 'border-box'
          }}
        />
      </div>

      {/* Activity Timeline */}
      <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-lg)', overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <div className="spinner" style={{ margin: '0 auto 1rem' }}></div>
            Loading activity...
          </div>
        ) : filteredLogs.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <Activity size={48} style={{ marginBottom: '1rem', opacity: 0.3 }} />
            <p style={{ margin: 0 }}>No activity logs yet. Actions will appear here automatically.</p>
          </div>
        ) : (
          filteredLogs.map((log, idx) => {
            const Icon = actionIcons[log.action] || Activity;
            const color = actionColors[log.action] || '#8b8fa3';
            return (
              <div key={log.id || idx} style={{
                display: 'flex', alignItems: 'flex-start', gap: '1rem',
                padding: '1rem 1.5rem', borderBottom: idx < filteredLogs.length - 1 ? '1px solid var(--border-color)' : 'none',
                transition: 'background 0.2s', cursor: 'default'
              }}
                onMouseEnter={e => e.currentTarget.style.background = 'rgba(79,142,247,0.03)'}
                onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
              >
                {/* Icon */}
                <div style={{
                  width: '36px', height: '36px', borderRadius: '50%',
                  background: `${color}18`, display: 'flex', alignItems: 'center',
                  justifyContent: 'center', flexShrink: 0, marginTop: '2px'
                }}>
                  <Icon size={16} color={color} />
                </div>

                {/* Content */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>{log.userName || 'System'}</span>
                    <span style={{
                      padding: '0.15rem 0.5rem', borderRadius: '2rem',
                      fontSize: '0.68rem', fontWeight: 600, textTransform: 'uppercase',
                      background: `${color}18`, color: color, letterSpacing: '0.03em'
                    }}>
                      {log.action}
                    </span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {log.entityType}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
                    {log.description || `${log.action} ${log.entityType}${log.entityName ? ': ' + log.entityName : ''}`}
                  </div>
                  {log.userEmail && (
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', opacity: 0.7 }}>
                      {log.userEmail}
                    </div>
                  )}
                </div>

                {/* Timestamp */}
                <div style={{ textAlign: 'right', flexShrink: 0 }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                    {getTimeAgo(log.timestamp)}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', opacity: 0.6, marginTop: '0.15rem' }}>
                    {new Date(log.timestamp).toLocaleTimeString()}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '1.5rem' }}>
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
            style={{
              display: 'flex', alignItems: 'center', gap: '0.4rem',
              padding: '0.5rem 1rem', background: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)',
              color: page === 0 ? 'var(--text-muted)' : 'var(--text-primary)',
              cursor: page === 0 ? 'not-allowed' : 'pointer', fontSize: '0.8rem'
            }}>
            <ChevronLeft size={14} /> Previous
          </button>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Page {page + 1} of {totalPages}
          </span>
          <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
            style={{
              display: 'flex', alignItems: 'center', gap: '0.4rem',
              padding: '0.5rem 1rem', background: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)',
              color: page >= totalPages - 1 ? 'var(--text-muted)' : 'var(--text-primary)',
              cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer', fontSize: '0.8rem'
            }}>
            Next <ChevronRight size={14} />
          </button>
        </div>
      )}
    </div>
  );
};

export default ActivityLog;
