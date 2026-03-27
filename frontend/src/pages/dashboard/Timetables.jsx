import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Calendar, Clock, Plus, Zap, Eye, Archive, CheckCircle, Trash2, X, ChevronRight } from 'lucide-react';

const DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'];
const DAY_LABELS = { MONDAY:'Mon', TUESDAY:'Tue', WEDNESDAY:'Wed', THURSDAY:'Thu', FRIDAY:'Fri' };

const Timetables = () => {
  const [timetables, setTimetables] = useState([]);
  const [selected, setSelected] = useState(null);
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type='success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  const fetchTimetables = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/v1/timetables');
      const list = res.data?.data?.content || res.data?.data || [];
      setTimetables(list);
    } catch { showToast('Failed to load timetables','error'); }
    finally { setLoading(false); }
  };

  const fetchEntries = async (ttId) => {
    try {
      const res = await api.get(`/api/v1/schedules/timetable/${ttId}`);
      setEntries(res.data?.data || []);
    } catch { setEntries([]); }
  };

  useEffect(() => { fetchTimetables(); }, []);

  const handleSelect = async (tt) => {
    setSelected(tt);
    await fetchEntries(tt.id);
  };

  const handleGenerateDemo = async () => {
    try {
      setGenerating(true);
      const createRes = await api.post('/api/v1/timetables', {
        name: `Spring 2026 Schedule #${Math.floor(Math.random()*100)}`,
        description: 'Auto-generated timetable with conflict detection.',
        effectiveFrom: '2026-04-01', effectiveTo: '2026-06-30',
        timetableType: 'WEEKLY',
        timeSlots: [
          { slotName: 'Slot A', startTime: '09:00', endTime: '10:00', dayOfWeek: 'MONDAY', slotType: 'REGULAR' },
          { slotName: 'Slot B', startTime: '10:15', endTime: '11:15', dayOfWeek: 'MONDAY', slotType: 'REGULAR' },
          { slotName: 'Slot A', startTime: '09:00', endTime: '10:00', dayOfWeek: 'TUESDAY', slotType: 'REGULAR' },
          { slotName: 'Slot B', startTime: '10:15', endTime: '11:15', dayOfWeek: 'TUESDAY', slotType: 'REGULAR' },
          { slotName: 'Slot A', startTime: '09:00', endTime: '10:00', dayOfWeek: 'WEDNESDAY', slotType: 'REGULAR' },
          { slotName: 'Slot B', startTime: '10:15', endTime: '11:15', dayOfWeek: 'WEDNESDAY', slotType: 'REGULAR' },
        ]
      });
      const newTT = createRes.data.data;
      await api.post(`/api/v1/timetables/${newTT.id}/generate`, {
        timetableId: newTT.id,
        classRequirements: [
          { title: 'Mathematics 101', periodsPerWeek: 2, durationMinutes: 60, entryType: 'CLASS' },
          { title: 'Advanced Physics', periodsPerWeek: 2, durationMinutes: 60, entryType: 'CLASS' },
          { title: 'Computer Science', periodsPerWeek: 2, durationMinutes: 60, entryType: 'CLASS' },
        ]
      });
      showToast('Timetable generated successfully!');
      fetchTimetables();
    } catch (err) {
      showToast(err.response?.data?.message || 'Generation failed', 'error');
    } finally { setGenerating(false); }
  };

  const handlePublish = async (id) => {
    try { await api.post(`/api/v1/timetables/${id}/publish`); showToast('Timetable published!'); fetchTimetables(); }
    catch (e) { showToast(e.response?.data?.message || 'Publish failed', 'error'); }
  };

  const handleArchive = async (id) => {
    try { await api.post(`/api/v1/timetables/${id}/archive`); showToast('Archived successfully'); fetchTimetables(); if (selected?.id === id) setSelected(null); }
    catch (e) { showToast('Archive failed', 'error'); }
  };

  const getStatusBadge = (tt) => {
    const s = tt.status || 'DRAFT';
    const map = { PUBLISHED: 'badge-published', DRAFT: 'badge-draft', ARCHIVED: 'badge-archived' };
    return <span className={`badge ${map[s] || 'badge-draft'}`}>{s}</span>;
  };

  // Build grid: rows = time labels, cols = days
  const buildGrid = () => {
    const slots = {};
    entries.forEach(e => {
      const day = e.dayOfWeek;
      const time = e.startDatetime ? new Date(e.startDatetime).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'}) : '09:00';
      if (!slots[time]) slots[time] = {};
      if (!slots[time][day]) slots[time][day] = [];
      slots[time][day].push(e);
    });
    return slots;
  };

  return (
    <div style={{ display: 'flex', height: '100%', overflow: 'hidden' }}>
      {/* Sidebar List */}
      <div style={{ width: '300px', borderRight: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', overflow: 'hidden', flexShrink: 0 }}>
        <div style={{ padding: '1.25rem', borderBottom: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>Timetables</h2>
          <button className="glass-button primary btn-sm" onClick={handleGenerateDemo} disabled={generating} style={{ fontSize: '0.78rem', padding: '0.4rem 0.75rem' }}>
            {generating ? <span className="spinner" style={{width:14,height:14}} /> : <><Zap size={13} /> Generate</>}
          </button>
        </div>
        <div style={{ overflowY: 'auto', flex: 1, padding: '0.75rem' }}>
          {loading ? (
            <div style={{ padding: '1rem' }}>
              {[1,2,3].map(i => <div key={i} className="skeleton" style={{ height: '70px', marginBottom: '0.75rem' }} />)}
            </div>
          ) : timetables.length === 0 ? (
            <div className="empty-state" style={{ padding: '2rem' }}>
              <div className="empty-icon"><Calendar size={28} color="var(--brand-primary)" /></div>
              <p style={{ fontSize: '0.85rem' }}>No timetables yet. Click Generate to create one!</p>
            </div>
          ) : (
            timetables.map(tt => (
              <div key={tt.id} onClick={() => handleSelect(tt)} style={{
                padding: '0.9rem', borderRadius: 'var(--radius-sm)', cursor: 'pointer', marginBottom: '0.5rem',
                background: selected?.id === tt.id ? 'rgba(79,142,247,0.12)' : 'rgba(255,255,255,0.03)',
                border: `1px solid ${selected?.id === tt.id ? 'rgba(79,142,247,0.3)' : 'var(--border-color)'}`,
                transition: 'all 0.2s'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.35rem' }}>
                  <div style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-primary)', flex: 1, marginRight: '0.5rem' }}>{tt.name}</div>
                  {getStatusBadge(tt)}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Clock size={11} /> {new Date(tt.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Main Detail Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {!selected ? (
          <div className="empty-state" style={{ flex: 1 }}>
            <div className="empty-icon"><Calendar size={36} color="var(--brand-primary)" /></div>
            <h3>Select a Timetable</h3>
            <p>Click a timetable on the left to view its schedule grid, or generate a new one.</p>
            <button className="btn btn-primary" onClick={handleGenerateDemo} disabled={generating}>
              {generating ? 'Generating...' : <><Zap size={16} /> Generate Demo Timetable</>}
            </button>
          </div>
        ) : (
          <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem' }}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                  <h2 style={{ fontSize: '1.4rem', margin: 0 }}>{selected.name}</h2>
                  {getStatusBadge(selected)}
                </div>
                <p style={{ margin: 0, fontSize: '0.875rem' }}>{selected.description || 'No description'}</p>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                  {selected.effectiveFrom && `${selected.effectiveFrom} → ${selected.effectiveTo || 'Ongoing'}`}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                {selected.status !== 'PUBLISHED' && (
                  <button className="glass-button success btn-sm" onClick={() => handlePublish(selected.id)}>
                    <CheckCircle size={14} /> Publish
                  </button>
                )}
                <button className="glass-button danger btn-sm" onClick={() => handleArchive(selected.id)}>
                  <Archive size={14} /> Archive
                </button>
                <button className="glass-button btn-sm" onClick={() => setSelected(null)}>
                  <X size={14} /> Close
                </button>
              </div>
            </div>

            {/* Schedule Grid */}
            <div className="glass-card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                <Calendar size={16} color="var(--brand-primary)" />
                <h3 style={{ fontSize: '0.95rem', margin: 0 }}>Weekly Schedule Grid</h3>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginLeft: 'auto' }}>{entries.length} entries</span>
              </div>
              {entries.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                  <Zap size={32} style={{ marginBottom: '1rem', opacity: 0.5 }} />
                  <p>No schedule entries yet. The AI generator will populate this grid.</p>
                </div>
              ) : (
                <div style={{ overflowX: 'auto' }}>
                  <div className="tt-grid" style={{ gridTemplateColumns: `80px repeat(${DAYS.length}, 1fr)`, minWidth: '600px' }}>
                    {/* Header Row */}
                    <div className="tt-cell tt-header" style={{ minHeight: '40px' }}>Time</div>
                    {DAYS.map(d => <div key={d} className="tt-cell tt-header" style={{ minHeight: '40px' }}>{DAY_LABELS[d]}</div>)}
                    {/* Data Rows */}
                    {Object.entries(buildGrid()).sort().map(([time, dayMap]) => (
                      <React.Fragment key={time}>
                        <div className="tt-cell tt-header" style={{ fontSize: '0.7rem', minHeight: '60px' }}>{time}</div>
                        {DAYS.map(day => (
                          <div key={day} className="tt-cell" style={{ minHeight: '60px', padding: '0.4rem' }}>
                            {(dayMap[day] || []).map((e, i) => (
                              <div key={i} className="tt-event" title={e.title}>{e.title}</div>
                            ))}
                          </div>
                        ))}
                      </React.Fragment>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Time Slots */}
            {selected.timeSlots && selected.timeSlots.length > 0 && (
              <div className="glass-card" style={{ marginTop: '1.25rem', padding: '1.25rem' }}>
                <h3 style={{ fontSize: '0.95rem', marginBottom: '1rem' }}>Defined Time Slots ({selected.timeSlots.length})</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '0.75rem' }}>
                  {selected.timeSlots.map((ts, i) => (
                    <div key={i} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.04)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                      <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>{ts.slotName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{ts.dayOfWeek} · {ts.startTime} - {ts.endTime}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Toast */}
      {toast && (
        <div className="toast-container">
          <div className={`toast toast-${toast.type}`}>{toast.msg}</div>
        </div>
      )}
    </div>
  );
};

export default Timetables;
