import React, { useEffect, useState, useCallback } from 'react';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import {
  UserX, UserCheck, Search, Plus, X, CheckCircle,
  AlertTriangle, Calendar, Clock, RefreshCw, ChevronDown
} from 'lucide-react';

// ── Helpers ────────────────────────────────────────────────────────────────
const fmt = (d) => d ? new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
const today = () => new Date().toISOString().split('T')[0];
const monthAgo = () => { const d = new Date(); d.setDate(d.getDate() - 30); return d.toISOString().split('T')[0]; };

const RESOLUTION_STYLE = {
  SUBSTITUTED: { bg: 'rgba(16,217,160,0.15)', color: 'var(--brand-accent)', label: 'Substituted' },
  CANCELLED:   { bg: 'rgba(247,79,110,0.15)', color: 'var(--brand-danger)',  label: 'Cancelled'   },
  PENDING:     { bg: 'rgba(247,169,79,0.15)', color: '#f7a94f',              label: 'Pending'     },
};

const avatarBg = (name) => {
  const colors = ['#4f8ef7','#9b72f7','#10d9a0','#f7a94f','#f74f6e'];
  return colors[(name || '').charCodeAt(0) % colors.length];
};

// ── Main Component ─────────────────────────────────────────────────────────
const AbsenceManagement = () => {
  const { user: currentUser } = useAuth();
  const orgId = currentUser?.organization?.id;

  // list state
  const [absences, setAbsences]   = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState(null);
  const [search, setSearch]       = useState('');
  const [from, setFrom]           = useState(monthAgo());
  const [to, setTo]               = useState(today());

  // modal state
  const [showModal, setShowModal]         = useState(false);
  const [showSubModal, setShowSubModal]   = useState(false);
  const [selectedAbsence, setSelected]    = useState(null);

  // substitutes finder
  const [substitutes, setSubstitutes]   = useState([]);
  const [subLoading, setSubLoading]     = useState(false);

  // teachers list (for dropdown)
  const [teachers, setTeachers] = useState([]);

  // toast
  const [toast, setToast] = useState(null);
  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  // form
  const blankForm = {
    teacherId: '', absentDate: today(), absenceType: 'FULL_DAY',
    partialFrom: '', partialTo: '', reason: '', substituteTeacherId: '', adminNotes: ''
  };
  const [form, setForm]       = useState(blankForm);
  const [submitting, setSubmitting] = useState(false);

  // ── Fetch absences ───────────────────────────────────────────────────────
  const fetchAbsences = useCallback(() => {
    if (!orgId) return;
    setLoading(true); setError(null);
    api.get(`/api/v1/absences?orgId=${orgId}&from=${from}&to=${to}`)
      .then(r => setAbsences(r.data?.data || []))
      .catch(e => setError(e.response?.data?.message || 'Failed to load absences'))
      .finally(() => setLoading(false));
  }, [orgId, from, to]);

  // ── Fetch team members for teacher dropdown ──────────────────────────────
  useEffect(() => {
    if (!orgId) return;
    api.get('/api/v1/users')
      .then(r => setTeachers(r.data?.data?.content || r.data?.data || []))
      .catch(() => {});
  }, [orgId]);

  useEffect(() => { fetchAbsences(); }, [fetchAbsences]);

  // ── Find available substitutes ──────────────────────────────────────────
  const findSubstitutes = async (teacherId, date) => {
    if (!teacherId || !date || !orgId) return;
    setSubLoading(true);
    try {
      const r = await api.get(`/api/v1/absences/substitutes?orgId=${orgId}&absentTeacherId=${teacherId}&date=${date}`);
      setSubstitutes(r.data?.data || []);
    } catch { setSubstitutes([]); }
    finally { setSubLoading(false); }
  };

  // ── Submit mark-absent form ──────────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = {
        organizationId: orgId,
        teacherId: form.teacherId,
        absentDate: form.absentDate,
        absenceType: form.absenceType,
        ...(form.absenceType === 'PARTIAL' && { partialFrom: form.partialFrom, partialTo: form.partialTo }),
        reason: form.reason || null,
        substituteTeacherId: form.substituteTeacherId || null,
        adminNotes: form.adminNotes || null,
      };
      const r = await api.post('/api/v1/absences', payload);
      if (r.data?.success) {
        showToast(`Absence recorded. ${r.data.data?.totalAffectedEntries || 0} entry/entries processed.`);
        setShowModal(false);
        setForm(blankForm);
        fetchAbsences();
      } else {
        showToast(r.data?.message || 'Failed to record absence', 'error');
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'An error occurred', 'error');
    } finally { setSubmitting(false); }
  };

  // ── Assign substitute to existing absence ──────────────────────────────
  const [subId, setSubId] = useState('');
  const handleAssignSub = async () => {
    if (!subId || !selectedAbsence) return;
    setSubmitting(true);
    try {
      const r = await api.patch(`/api/v1/absences/${selectedAbsence.id}/substitute?substituteId=${subId}`);
      if (r.data?.success) {
        showToast('Substitute assigned successfully');
        setShowSubModal(false); setSelected(null); setSubId('');
        fetchAbsences();
      } else { showToast(r.data?.message || 'Failed', 'error'); }
    } catch (err) {
      showToast(err.response?.data?.message || 'Error assigning substitute', 'error');
    } finally { setSubmitting(false); }
  };

  // ── Revert absence ─────────────────────────────────────────────────────
  const handleRevert = async (absence) => {
    if (!window.confirm(`Revert absence for ${absence.teacherName} on ${fmt(absence.absentDate)}? All entries will be restored.`)) return;
    try {
      await api.delete(`/api/v1/absences/${absence.id}`);
      showToast('Absence reverted — entries restored to SCHEDULED');
      fetchAbsences();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to revert', 'error');
    }
  };

  // ── Filtered list ──────────────────────────────────────────────────────
  const filtered = absences.filter(a =>
    `${a.teacherName} ${a.teacherEmail} ${a.substituteTeacherName || ''}`.toLowerCase().includes(search.toLowerCase())
  );

  // ── Stats ──────────────────────────────────────────────────────────────
  const stats = {
    total:       absences.length,
    substituted: absences.filter(a => a.resolution === 'SUBSTITUTED').length,
    cancelled:   absences.filter(a => a.resolution === 'CANCELLED').length,
  };

  // ══════════════════════════════════════════════════════════════════════════
  return (
    <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>

      {/* ── Page header ─────────────────────────────────────────────────── */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem', margin: 0, marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <UserX size={22} color="var(--brand-primary)" /> Teacher Absence Management
          </h2>
          <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Mark absences, assign substitutes, and auto-update the timetable.
          </p>
        </div>
        <button className="glass-button primary" onClick={() => { setForm(blankForm); setSubstitutes([]); setShowModal(true); }}>
          <Plus size={15} /> Mark Absent
        </button>
      </div>

      {/* ── Stats bar ───────────────────────────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: '1rem', marginBottom: '1.5rem' }}>
        {[
          { label: 'Total Absences',  value: stats.total,       icon: <Calendar size={18} />, color: 'var(--brand-primary)' },
          { label: 'Substituted',     value: stats.substituted, icon: <UserCheck size={18} />, color: 'var(--brand-accent)' },
          { label: 'Cancelled',       value: stats.cancelled,   icon: <AlertTriangle size={18} />, color: 'var(--brand-danger)' },
        ].map(s => (
          <div key={s.label} className="glass-card" style={{ padding: '1.1rem 1.4rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ width: 42, height: 42, borderRadius: '50%', background: 'rgba(255,255,255,0.06)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: s.color }}>
              {s.icon}
            </div>
            <div>
              <div style={{ fontSize: '1.6rem', fontWeight: 700, lineHeight: 1 }}>{s.value}</div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>{s.label}</div>
            </div>
          </div>
        ))}
      </div>

      {/* ── Filters ─────────────────────────────────────────────────────── */}
      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.25rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ position: 'relative', flex: '1 1 200px' }}>
          <Search size={14} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input className="input-field" placeholder="Search teacher or substitute..." value={search} onChange={e => setSearch(e.target.value)}
            style={{ paddingLeft: '2.2rem', fontSize: '0.875rem', width: '100%' }} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          <Calendar size={14} />
          <input type="date" className="input-field" value={from} onChange={e => setFrom(e.target.value)} style={{ fontSize: '0.83rem', padding: '0.48rem 0.75rem' }} />
          <span>to</span>
          <input type="date" className="input-field" value={to} onChange={e => setTo(e.target.value)} style={{ fontSize: '0.83rem', padding: '0.48rem 0.75rem' }} />
        </div>
        <button className="glass-button" onClick={fetchAbsences} title="Refresh">
          <RefreshCw size={14} />
        </button>
      </div>

      {error && (
        <div style={{ color: 'var(--brand-danger)', marginBottom: '1rem', padding: '0.75rem 1rem', background: 'rgba(247,79,110,0.1)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(247,79,110,0.2)' }}>
          {error}
        </div>
      )}

      {/* ── Absence list ─────────────────────────────────────────────────── */}
      {loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {[1,2,3].map(i => <div key={i} className="skeleton" style={{ height: '90px', borderRadius: 'var(--radius-md)' }} />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="glass-card empty-state">
          <div className="empty-icon"><UserX size={32} color="var(--text-muted)" /></div>
          <h3>{search ? 'No absences found' : 'No Absences Recorded'}</h3>
          <p>{search ? 'Try a different search term.' : 'Mark a teacher as absent to get started.'}</p>
          {!search && <button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={15} /> Mark First Absence</button>}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {filtered.map(abs => {
            const res = RESOLUTION_STYLE[abs.resolution] || RESOLUTION_STYLE.PENDING;
            const initials = (abs.teacherName || 'T').split(' ').map(n => n[0]).join('').slice(0,2).toUpperCase();
            return (
              <div key={abs.id} className="glass-card" style={{ padding: '1.25rem', display: 'flex', alignItems: 'center', gap: '1.25rem', flexWrap: 'wrap' }}>
                {/* Avatar */}
                <div className="avatar" style={{ background: avatarBg(abs.teacherName), width: 46, height: 46, fontSize: '1rem', flexShrink: 0 }}>
                  {initials}
                </div>

                {/* Main info */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontWeight: 700, fontSize: '1rem', marginBottom: '0.3rem' }}>{abs.teacherName}</div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.9rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Calendar size={12} /> {fmt(abs.absentDate)}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Clock size={12} /> {abs.absenceType === 'PARTIAL' ? `${abs.partialFrom} – ${abs.partialTo}` : 'Full Day'}
                    </span>
                    {abs.reason && <span>📝 {abs.reason}</span>}
                    <span>{abs.totalAffectedEntries} slot(s) affected</span>
                  </div>
                  {abs.substituteTeacherName && (
                    <div style={{ marginTop: '0.4rem', fontSize: '0.8rem', color: 'var(--brand-accent)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                      <UserCheck size={12} /> Covered by <strong>{abs.substituteTeacherName}</strong>
                    </div>
                  )}
                </div>

                {/* Resolution badge + actions */}
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.6rem', flexShrink: 0 }}>
                  <span style={{ padding: '0.25rem 0.8rem', borderRadius: '2rem', fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', background: res.bg, color: res.color }}>
                    {res.label}
                  </span>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    {abs.resolution !== 'SUBSTITUTED' && (
                      <button className="glass-button btn-sm" style={{ fontSize: '0.75rem', padding: '0.3rem 0.65rem' }}
                        onClick={() => { setSelected(abs); findSubstitutes(abs.teacherId, abs.absentDate); setShowSubModal(true); }}>
                        <UserCheck size={12} /> Assign Sub
                      </button>
                    )}
                    <button className="glass-button btn-sm" style={{ fontSize: '0.75rem', padding: '0.3rem 0.65rem', color: 'var(--brand-danger)' }}
                      onClick={() => handleRevert(abs)}>
                      <RefreshCw size={12} /> Revert
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ── Toast ───────────────────────────────────────────────────────── */}
      {toast && (
        <div className="toast-container">
          <div className={`toast toast-${toast.type}`}>{toast.msg}</div>
        </div>
      )}

      {/* ════════════════════════════════════════════════════════════════════
          Modal: Mark Teacher Absent
      ═══════════════════════════════════════════════════════════════════════ */}
      {showModal && (
        <div className="modal-overlay" style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.55)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: 480, padding: '2rem', margin: '1rem', position: 'relative', maxHeight: '90vh', overflowY: 'auto' }}>
            <button onClick={() => setShowModal(false)} style={{ position: 'absolute', top: '1.2rem', right: '1.2rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={20} /></button>

            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.3rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <UserX size={18} color="var(--brand-danger)" /> Mark Teacher Absent
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Affected timetable entries will be cancelled or reassigned automatically.
            </p>

            <form onSubmit={handleSubmit}>
              {/* Teacher */}
              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label">Teacher *</label>
                <select className="input-field" required value={form.teacherId}
                  onChange={e => { setForm({ ...form, teacherId: e.target.value }); findSubstitutes(e.target.value, form.absentDate); }}>
                  <option value="">— Select teacher —</option>
                  {teachers.map(t => <option key={t.id} value={t.id}>{t.firstName} {t.lastName} ({t.email})</option>)}
                </select>
              </div>

              {/* Date */}
              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label">Absent Date *</label>
                <input type="date" className="input-field" required value={form.absentDate}
                  onChange={e => { setForm({ ...form, absentDate: e.target.value }); findSubstitutes(form.teacherId, e.target.value); }} />
              </div>

              {/* Absence type */}
              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label">Absence Type</label>
                <select className="input-field" value={form.absenceType} onChange={e => setForm({ ...form, absenceType: e.target.value })}>
                  <option value="FULL_DAY">Full Day</option>
                  <option value="PARTIAL">Partial (specific hours)</option>
                </select>
              </div>

              {/* Partial time window */}
              {form.absenceType === 'PARTIAL' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="input-group">
                    <label className="input-label">From Time</label>
                    <input type="time" className="input-field" value={form.partialFrom} onChange={e => setForm({ ...form, partialFrom: e.target.value })} />
                  </div>
                  <div className="input-group">
                    <label className="input-label">To Time</label>
                    <input type="time" className="input-field" value={form.partialTo} onChange={e => setForm({ ...form, partialTo: e.target.value })} />
                  </div>
                </div>
              )}

              {/* Reason */}
              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label">Reason (optional)</label>
                <input type="text" className="input-field" placeholder="e.g. Medical leave, Conference..." value={form.reason} onChange={e => setForm({ ...form, reason: e.target.value })} />
              </div>

              {/* Substitute */}
              <div className="input-group" style={{ marginBottom: '1rem' }}>
                <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  Substitute Teacher
                  {subLoading && <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Finding available…</span>}
                  {!subLoading && substitutes.length > 0 && <span style={{ fontSize: '0.75rem', color: 'var(--brand-accent)' }}>{substitutes.length} available</span>}
                  {!subLoading && form.teacherId && substitutes.length === 0 && <span style={{ fontSize: '0.75rem', color: 'var(--brand-danger)' }}>None free</span>}
                </label>
                <select className="input-field" value={form.substituteTeacherId} onChange={e => setForm({ ...form, substituteTeacherId: e.target.value })}>
                  <option value="">— Cancel all slots (no substitute) —</option>
                  {substitutes.map(s => <option key={s.id} value={s.id}>{s.firstName} {s.lastName} ({s.email})</option>)}
                </select>
                {!form.substituteTeacherId && (
                  <span style={{ fontSize: '0.75rem', color: 'var(--brand-danger)', marginTop: '0.3rem', display: 'block' }}>
                    ⚠ All affected entries will be CANCELLED if no substitute is selected.
                  </span>
                )}
              </div>

              {/* Admin notes */}
              <div className="input-group" style={{ marginBottom: '1.75rem' }}>
                <label className="input-label">Admin Notes (internal)</label>
                <textarea className="input-field" rows={2} placeholder="Internal notes, not shown to students..." value={form.adminNotes} onChange={e => setForm({ ...form, adminNotes: e.target.value })} style={{ resize: 'vertical' }} />
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)} disabled={submitting}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {submitting ? <div className="spinner" style={{ width: 16, height: 16 }} /> : <CheckCircle size={16} />}
                  Confirm Absence
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ════════════════════════════════════════════════════════════════════
          Modal: Assign Substitute to Existing Absence
      ═══════════════════════════════════════════════════════════════════════ */}
      {showSubModal && selectedAbsence && (
        <div className="modal-overlay" style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.55)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: 420, padding: '2rem', margin: '1rem', position: 'relative' }}>
            <button onClick={() => { setShowSubModal(false); setSelected(null); }} style={{ position: 'absolute', top: '1.2rem', right: '1.2rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={20} /></button>

            <h3 style={{ fontSize: '1.15rem', marginBottom: '0.3rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <UserCheck size={18} color="var(--brand-accent)" /> Assign Substitute
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.84rem', marginBottom: '1.5rem' }}>
              For <strong>{selectedAbsence.teacherName}</strong> on <strong>{fmt(selectedAbsence.absentDate)}</strong>
            </p>

            <div className="input-group" style={{ marginBottom: '1.5rem' }}>
              <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                Available Substitutes
                {subLoading && <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Loading…</span>}
                {!subLoading && <span style={{ fontSize: '0.75rem', color: 'var(--brand-accent)' }}>{substitutes.length} free</span>}
              </label>
              <select className="input-field" value={subId} onChange={e => setSubId(e.target.value)}>
                <option value="">— Select substitute —</option>
                {substitutes.map(s => <option key={s.id} value={s.id}>{s.firstName} {s.lastName}</option>)}
                {!subLoading && substitutes.length === 0 && <option disabled>No free teachers found</option>}
              </select>
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" onClick={() => { setShowSubModal(false); setSelected(null); }} disabled={submitting}>Cancel</button>
              <button className="btn btn-primary" onClick={handleAssignSub} disabled={submitting || !subId} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                {submitting ? <div className="spinner" style={{ width: 16, height: 16 }} /> : <UserCheck size={16} />}
                Assign
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AbsenceManagement;
