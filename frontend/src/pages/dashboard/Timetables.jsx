import React, { useEffect, useState, useCallback } from 'react';
import api from '../../services/api';
import {
  Calendar, Clock, Plus, Zap, CheckCircle, Archive, Trash2, X,
  AlertTriangle, BookOpen, Users, MapPin, ChevronRight, ChevronLeft,
  RefreshCw, Eye, Copy, GripVertical, Info, Download, FileText
} from 'lucide-react';

/* ─── Constants ─────────────────────────────────────────── */
const DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'];
const DAY_LABELS = { MONDAY:'Mon',TUESDAY:'Tue',WEDNESDAY:'Wed',THURSDAY:'Thu',FRIDAY:'Fri',SATURDAY:'Sat' };

const SUBJECT_COLORS = [
  { bg:'rgba(79,70,229,0.18)', border:'rgba(79,70,229,0.5)', text:'#a5b4fc', hex:'#4F46E5' },
  { bg:'rgba(6,182,212,0.18)', border:'rgba(6,182,212,0.5)', text:'#67e8f9', hex:'#06b6d4' },
  { bg:'rgba(16,185,129,0.18)', border:'rgba(16,185,129,0.5)', text:'#6ee7b7', hex:'#10b981' },
  { bg:'rgba(245,158,11,0.18)', border:'rgba(245,158,11,0.5)', text:'#fcd34d', hex:'#f59e0b' },
  { bg:'rgba(239,68,68,0.18)', border:'rgba(239,68,68,0.5)', text:'#fca5a5', hex:'#ef4444' },
  { bg:'rgba(168,85,247,0.18)', border:'rgba(168,85,247,0.5)', text:'#d8b4fe', hex:'#a855f7' },
  { bg:'rgba(236,72,153,0.18)', border:'rgba(236,72,153,0.5)', text:'#f9a8d4', hex:'#ec4899' },
  { bg:'rgba(20,184,166,0.18)', border:'rgba(20,184,166,0.5)', text:'#5eead4', hex:'#14b8a6' },
];

const DEFAULT_TIMESLOTS = [
  { slotName:'Period 1', startTime:'08:00', endTime:'09:00', dayOfWeek:'MONDAY', slotType:'REGULAR' },
  { slotName:'Period 2', startTime:'09:15', endTime:'10:15', dayOfWeek:'MONDAY', slotType:'REGULAR' },
  { slotName:'Period 3', startTime:'10:30', endTime:'11:30', dayOfWeek:'MONDAY', slotType:'REGULAR' },
  { slotName:'Break',    startTime:'11:30', endTime:'11:45', dayOfWeek:'MONDAY', slotType:'BREAK' },
  { slotName:'Period 4', startTime:'11:45', endTime:'12:45', dayOfWeek:'MONDAY', slotType:'REGULAR' },
  { slotName:'Lunch',    startTime:'12:45', endTime:'13:30', dayOfWeek:'MONDAY', slotType:'LUNCH' },
  { slotName:'Period 5', startTime:'13:30', endTime:'14:30', dayOfWeek:'MONDAY', slotType:'REGULAR' },
  { slotName:'Period 6', startTime:'14:45', endTime:'15:45', dayOfWeek:'MONDAY', slotType:'REGULAR' },
];

const ENTRY_TYPES = ['CLASS','LECTURE','LAB','TUTORIAL','SEMINAR','EXAMINATION'];

/* ─── Sub-components ─────────────────────────────────────── */
const Toast = ({ toast }) => toast ? (
  <div className="toast-container">
    <div className={`toast toast-${toast.type}`}>
      {toast.type === 'error' ? <AlertTriangle size={16}/> : <CheckCircle size={16}/>}
      {toast.msg}
    </div>
  </div>
) : null;

const StatusBadge = ({ status }) => {
  const s = status || 'DRAFT';
  const map = { PUBLISHED:'badge-published', DRAFT:'badge-draft', ARCHIVED:'badge-archived', PENDING_APPROVAL:'badge-draft', APPROVED:'badge-active' };
  return <span className={`badge ${map[s]||'badge-draft'}`}>{s.replace('_',' ')}</span>;
};

/* ─── Wizard Step 1: Basic Info ──────────────────────────── */
const StepBasicInfo = ({ form, setForm }) => (
  <div style={{ display:'flex', flexDirection:'column', gap:'1rem' }}>
    <div className="input-group" style={{ marginBottom:0 }}>
      <label className="input-label">Timetable Name *</label>
      <input className="input-field" placeholder="e.g. Semester 1 — CS Department 2026"
        value={form.name} onChange={e => setForm(f=>({...f, name:e.target.value}))} />
    </div>
    <div className="input-group" style={{ marginBottom:0 }}>
      <label className="input-label">Description</label>
      <textarea className="input-field" rows={2} placeholder="Optional description..."
        value={form.description} onChange={e => setForm(f=>({...f, description:e.target.value}))}
        style={{ resize:'vertical' }} />
    </div>
    <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:'1rem' }}>
      <div className="input-group" style={{ marginBottom:0 }}>
        <label className="input-label">Effective From *</label>
        <input type="date" className="input-field" value={form.effectiveFrom}
          onChange={e => setForm(f=>({...f, effectiveFrom:e.target.value}))} />
      </div>
      <div className="input-group" style={{ marginBottom:0 }}>
        <label className="input-label">Effective To</label>
        <input type="date" className="input-field" value={form.effectiveTo}
          onChange={e => setForm(f=>({...f, effectiveTo:e.target.value}))} />
      </div>
    </div>
    <div className="input-group" style={{ marginBottom:0 }}>
      <label className="input-label">Timetable Type</label>
      <select className="input-field" value={form.timetableType}
        onChange={e => setForm(f=>({...f, timetableType:e.target.value}))}>
        <option value="WEEKLY">Weekly (Mon–Fri)</option>
        <option value="DAILY">Daily</option>
        <option value="CUSTOM">Custom</option>
        <option value="ROTATING">Rotating</option>
      </select>
    </div>
  </div>
);

/* ─── Wizard Step 2: Time Slots ──────────────────────────── */
const StepTimeSlots = ({ slots, setSlots }) => {
  const addSlot = () => setSlots(s => [...s, { slotName:'', startTime:'08:00', endTime:'09:00', dayOfWeek:'MONDAY', slotType:'REGULAR' }]);
  const remove = (i) => setSlots(s => s.filter((_,j)=>j!==i));
  const update = (i, key, val) => setSlots(s => s.map((sl,j) => j===i ? {...sl,[key]:val} : sl));
  const applyToAllDays = () => {
    const base = slots.filter(s=>s.dayOfWeek==='MONDAY');
    const newSlots = [];
    ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'].forEach(day => {
      base.forEach(s => newSlots.push({...s, dayOfWeek:day}));
    });
    setSlots(newSlots);
  };
  return (
    <div>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'0.75rem' }}>
        <p style={{ fontSize:'0.85rem', color:'var(--text-muted)', margin:0 }}>Define your weekly time periods</p>
        <div style={{ display:'flex', gap:'0.5rem' }}>
          <button className="glass-button btn-sm" onClick={applyToAllDays} style={{ fontSize:'0.75rem' }}>
            <Copy size={12}/> Apply Mon→Fri
          </button>
          <button className="glass-button primary btn-sm" onClick={addSlot} style={{ fontSize:'0.75rem' }}>
            <Plus size={12}/> Add Slot
          </button>
        </div>
      </div>
      <div style={{ maxHeight:'340px', overflowY:'auto', display:'flex', flexDirection:'column', gap:'0.5rem' }}>
        {slots.map((sl, i) => (
          <div key={i} style={{ display:'grid', gridTemplateColumns:'1.5fr 100px 100px 110px 100px 32px', gap:'0.4rem', alignItems:'center', padding:'0.5rem 0.75rem', background:'rgba(255,255,255,0.03)', borderRadius:'var(--radius-sm)', border:'1px solid var(--border-color)' }}>
            <input className="input-field" style={{ padding:'0.4rem 0.6rem', fontSize:'0.8rem' }} placeholder="Slot name"
              value={sl.slotName} onChange={e=>update(i,'slotName',e.target.value)} />
            <input type="time" className="input-field" style={{ padding:'0.4rem 0.5rem', fontSize:'0.8rem' }}
              value={sl.startTime} onChange={e=>update(i,'startTime',e.target.value)} />
            <input type="time" className="input-field" style={{ padding:'0.4rem 0.5rem', fontSize:'0.8rem' }}
              value={sl.endTime} onChange={e=>update(i,'endTime',e.target.value)} />
            <select className="input-field" style={{ padding:'0.4rem 0.5rem', fontSize:'0.8rem' }}
              value={sl.dayOfWeek} onChange={e=>update(i,'dayOfWeek',e.target.value)}>
              {['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'].map(d=>(
                <option key={d} value={d}>{DAY_LABELS[d]}</option>
              ))}
            </select>
            <select className="input-field" style={{ padding:'0.4rem 0.5rem', fontSize:'0.8rem' }}
              value={sl.slotType} onChange={e=>update(i,'slotType',e.target.value)}>
              <option value="REGULAR">Regular</option>
              <option value="BREAK">Break</option>
              <option value="LUNCH">Lunch</option>
            </select>
            <button onClick={()=>remove(i)} style={{ background:'none', border:'none', color:'var(--brand-danger)', cursor:'pointer', padding:'0.25rem', display:'flex' }}>
              <X size={14}/>
            </button>
          </div>
        ))}
        {slots.length===0 && (
          <div style={{ textAlign:'center', padding:'2rem', color:'var(--text-muted)', fontSize:'0.85rem' }}>
            No time slots yet. Click <strong>Add Slot</strong> or use defaults.
          </div>
        )}
      </div>
    </div>
  );
};

/* ─── Wizard Step 3: Subjects ────────────────────────────── */
const StepSubjects = ({ subjects, setSubjects, teamMembers, resources }) => {
  const add = () => setSubjects(s=>[...s,{title:'', periodsPerWeek:2, durationMinutes:60, entryType:'CLASS', teacherId:'', resourceId:'', colorIdx: s.length % SUBJECT_COLORS.length}]);
  const remove = (i) => setSubjects(s=>s.filter((_,j)=>j!==i));
  const update = (i,key,val) => setSubjects(s=>s.map((x,j)=>j===i?{...x,[key]:val}:x));
  return (
    <div>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'0.75rem' }}>
        <p style={{ fontSize:'0.85rem', color:'var(--text-muted)', margin:0 }}>Add subjects/courses to schedule</p>
        <button className="glass-button primary btn-sm" onClick={add} style={{ fontSize:'0.75rem' }}>
          <Plus size={12}/> Add Subject
        </button>
      </div>
      <div style={{ maxHeight:'360px', overflowY:'auto', display:'flex', flexDirection:'column', gap:'0.75rem' }}>
        {subjects.map((s,i) => {
          const col = SUBJECT_COLORS[s.colorIdx ?? i % SUBJECT_COLORS.length];
          return (
            <div key={i} style={{ padding:'0.9rem', background:'rgba(255,255,255,0.03)', borderRadius:'var(--radius-sm)', border:`1px solid ${col.border}`, position:'relative' }}>
              <div style={{ position:'absolute', top:'0.75rem', left:'0.6rem', width:'4px', height:'calc(100% - 1.5rem)', borderRadius:'2px', background:col.hex }} />
              <div style={{ marginLeft:'0.75rem' }}>
                <div style={{ display:'grid', gridTemplateColumns:'1fr 80px 80px 100px', gap:'0.5rem', marginBottom:'0.5rem', alignItems:'center' }}>
                  <input className="input-field" style={{ padding:'0.4rem 0.6rem', fontSize:'0.85rem' }} placeholder="Subject title *"
                    value={s.title} onChange={e=>update(i,'title',e.target.value)} />
                  <div>
                    <label style={{ fontSize:'0.7rem', color:'var(--text-muted)' }}>Periods/wk</label>
                    <input type="number" className="input-field" style={{ padding:'0.35rem 0.5rem', fontSize:'0.8rem' }} min={1} max={10}
                      value={s.periodsPerWeek} onChange={e=>update(i,'periodsPerWeek',+e.target.value)} />
                  </div>
                  <div>
                    <label style={{ fontSize:'0.7rem', color:'var(--text-muted)' }}>Duration</label>
                    <input type="number" className="input-field" style={{ padding:'0.35rem 0.5rem', fontSize:'0.8rem' }} min={15} step={15}
                      value={s.durationMinutes} onChange={e=>update(i,'durationMinutes',+e.target.value)} />
                  </div>
                  <div>
                    <label style={{ fontSize:'0.7rem', color:'var(--text-muted)' }}>Type</label>
                    <select className="input-field" style={{ padding:'0.35rem 0.5rem', fontSize:'0.8rem' }}
                      value={s.entryType} onChange={e=>update(i,'entryType',e.target.value)}>
                      {ENTRY_TYPES.map(t=><option key={t} value={t}>{t}</option>)}
                    </select>
                  </div>
                </div>
                <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.5rem' }}>
                  <div>
                    <label style={{ fontSize:'0.7rem', color:'var(--text-muted)' }}><Users size={11}/> Instructor</label>
                    <select className="input-field" style={{ padding:'0.35rem 0.5rem', fontSize:'0.8rem' }}
                      value={s.teacherId} onChange={e=>update(i,'teacherId',e.target.value)}>
                      <option value="">— Unassigned —</option>
                      {teamMembers.map(m=><option key={m.id} value={m.id}>{m.fullName||m.email}</option>)}
                    </select>
                  </div>
                  <div>
                    <label style={{ fontSize:'0.7rem', color:'var(--text-muted)' }}><MapPin size={11}/> Room / Resource</label>
                    <select className="input-field" style={{ padding:'0.35rem 0.5rem', fontSize:'0.8rem' }}
                      value={s.resourceId} onChange={e=>update(i,'resourceId',e.target.value)}>
                      <option value="">— Any Room —</option>
                      {resources.map(r=><option key={r.id} value={r.id}>{r.name}{r.code?` (${r.code})`:''}</option>)}
                    </select>
                  </div>
                </div>
              </div>
              <button onClick={()=>remove(i)} style={{ position:'absolute', top:'0.5rem', right:'0.5rem', background:'none', border:'none', color:'var(--brand-danger)', cursor:'pointer', padding:'0.2rem', opacity:0.7 }}>
                <X size={14}/>
              </button>
            </div>
          );
        })}
        {subjects.length===0 && (
          <div style={{ textAlign:'center', padding:'2rem', color:'var(--text-muted)', fontSize:'0.85rem' }}>
            No subjects yet. Add subjects to generate schedule.
          </div>
        )}
      </div>
    </div>
  );
};

/* ─── Create Wizard Modal ────────────────────────────────── */
const CreateWizard = ({ onClose, onSuccess, teamMembers, resources, showToast }) => {
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    name: '', description: '', effectiveFrom: new Date().toISOString().split('T')[0],
    effectiveTo: '', timetableType: 'WEEKLY'
  });
  const [slots, setSlots] = useState(DEFAULT_TIMESLOTS);
  const [subjects, setSubjects] = useState([
    { title:'', periodsPerWeek:2, durationMinutes:60, entryType:'CLASS', teacherId:'', resourceId:'', colorIdx:0 }
  ]);
  const [conflicts, setConflicts] = useState([]);

  const steps = ['Basic Info', 'Time Slots', 'Subjects', 'Preview & Generate'];
  const canNext = () => {
    if (step===0) return form.name.trim() && form.effectiveFrom;
    if (step===1) return slots.length > 0;
    if (step===2) return subjects.length > 0 && subjects.every(s=>s.title.trim());
    return true;
  };

  const handleGenerate = async () => {
    setLoading(true);
    setConflicts([]);
    try {
      // Step 1: Create timetable with time slots
      const createRes = await api.post('/api/v1/timetables', {
        name: form.name, description: form.description,
        effectiveFrom: form.effectiveFrom, effectiveTo: form.effectiveTo || undefined,
        timetableType: form.timetableType, timeSlots: slots
      });
      const tt = createRes.data.data;

      // Step 2: Generate schedule entries
      const genRes = await api.post(`/api/v1/timetables/${tt.id}/generate`, {
        timetableId: tt.id,
        classRequirements: subjects.map(s=>({
          title: s.title, periodsPerWeek: s.periodsPerWeek,
          durationMinutes: s.durationMinutes, entryType: s.entryType,
          teacherId: s.teacherId || undefined, resourceId: s.resourceId || undefined
        }))
      });

      // Check for partial scheduling warnings
      const generated = genRes.data.data || [];
      const totalNeeded = subjects.reduce((a,s)=>a+s.periodsPerWeek, 0);
      if (generated.length < totalNeeded) {
        setConflicts([`⚠ Only ${generated.length}/${totalNeeded} periods scheduled. Some slots had conflicts.`]);
      }

      showToast(`✅ Timetable created with ${generated.length} schedule entries!`);
      onSuccess(tt);
    } catch(err) {
      const msg = err.response?.data?.message || err.message || 'Generation failed';
      showToast(msg, 'error');
    } finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e=>e.target===e.currentTarget && onClose()}>
      <div className="modal-box" style={{ maxWidth:'680px' }}>
        {/* Header */}
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'1.5rem' }}>
          <div>
            <h2 className="modal-title">Create Timetable</h2>
            <p style={{ margin:0, fontSize:'0.82rem', color:'var(--text-muted)' }}>Step {step+1} of {steps.length}: {steps[step]}</p>
          </div>
          <button onClick={onClose} style={{ background:'none', border:'none', color:'var(--text-muted)', cursor:'pointer' }}><X size={20}/></button>
        </div>

        {/* Step progress */}
        <div style={{ display:'flex', gap:'0.35rem', marginBottom:'1.5rem' }}>
          {steps.map((s,i)=>(
            <div key={i} style={{ flex:1, height:'4px', borderRadius:'2px',
              background: i<=step ? 'var(--brand-gradient)' : 'var(--border-color)',
              transition:'background 0.3s ease' }} />
          ))}
        </div>

        {/* Step content */}
        <div style={{ minHeight:'320px' }}>
          {step===0 && <StepBasicInfo form={form} setForm={setForm}/>}
          {step===1 && <StepTimeSlots slots={slots} setSlots={setSlots}/>}
          {step===2 && <StepSubjects subjects={subjects} setSubjects={setSubjects} teamMembers={teamMembers} resources={resources}/>}
          {step===3 && (
            <div style={{ display:'flex', flexDirection:'column', gap:'1rem' }}>
              <div style={{ padding:'1rem', background:'rgba(79,70,229,0.1)', border:'1px solid rgba(79,70,229,0.3)', borderRadius:'var(--radius-sm)' }}>
                <h4 style={{ margin:'0 0 0.5rem', fontSize:'0.9rem' }}>📋 Summary</h4>
                <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.35rem', fontSize:'0.82rem', color:'var(--text-secondary)' }}>
                  <span><strong>Name:</strong> {form.name}</span>
                  <span><strong>Type:</strong> {form.timetableType}</span>
                  <span><strong>From:</strong> {form.effectiveFrom}</span>
                  <span><strong>To:</strong> {form.effectiveTo||'Open-ended'}</span>
                  <span><strong>Time Slots:</strong> {slots.length}</span>
                  <span><strong>Subjects:</strong> {subjects.length}</span>
                </div>
              </div>
              <div>
                <h4 style={{ fontSize:'0.85rem', marginBottom:'0.5rem' }}>Subjects to Schedule</h4>
                {subjects.map((s,i)=>{
                  const col = SUBJECT_COLORS[s.colorIdx ?? i%SUBJECT_COLORS.length];
                  return (
                    <div key={i} style={{ display:'flex', alignItems:'center', gap:'0.75rem', padding:'0.5rem 0.75rem', marginBottom:'0.35rem', background:'rgba(255,255,255,0.03)', borderRadius:'var(--radius-sm)', borderLeft:`3px solid ${col.hex}` }}>
                      <BookOpen size={14} color={col.text}/>
                      <span style={{ flex:1, fontSize:'0.85rem' }}>{s.title}</span>
                      <span style={{ fontSize:'0.75rem', color:'var(--text-muted)' }}>{s.periodsPerWeek} periods/wk · {s.durationMinutes}min · {s.entryType}</span>
                    </div>
                  );
                })}
              </div>
              {conflicts.length>0 && (
                <div style={{ padding:'0.75rem', background:'rgba(245,158,11,0.12)', border:'1px solid rgba(245,158,11,0.3)', borderRadius:'var(--radius-sm)' }}>
                  {conflicts.map((c,i)=><p key={i} style={{ margin:0, fontSize:'0.82rem', color:'var(--brand-warning)' }}>{c}</p>)}
                </div>
              )}
              <p style={{ fontSize:'0.82rem', color:'var(--text-muted)', margin:0 }}>
                <Info size={13} style={{ marginRight:'0.3rem', verticalAlign:'middle' }}/>
                The AI scheduler will assign subjects to available time slots using conflict detection. Rooms and instructors will be respected if assigned.
              </p>
            </div>
          )}
        </div>

        {/* Footer actions */}
        <div style={{ display:'flex', justifyContent:'space-between', marginTop:'1.5rem', paddingTop:'1rem', borderTop:'1px solid var(--border-color)' }}>
          <button className="btn btn-secondary btn-sm" onClick={()=>step>0 ? setStep(s=>s-1) : onClose()}>
            <ChevronLeft size={15}/> {step===0 ? 'Cancel' : 'Back'}
          </button>
          {step < steps.length-1 ? (
            <button className="btn btn-primary btn-sm" disabled={!canNext()} onClick={()=>setStep(s=>s+1)}>
              Next <ChevronRight size={15}/>
            </button>
          ) : (
            <button className="btn btn-primary btn-sm" disabled={loading} onClick={handleGenerate}>
              {loading ? <><span className="spinner" style={{width:16,height:16}}/> Generating...</> : <><Zap size={15}/> Generate Timetable</>}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

/* ─── Visual Weekly Grid ─────────────────────────────────── */
const WeeklyGrid = ({ entries, activeDays }) => {
  const [selected, setSelected] = useState(null);
  const colorMap = {};
  const uniqueTitles = [...new Set(entries.map(e=>e.title))];
  uniqueTitles.forEach((t,i) => { colorMap[t] = SUBJECT_COLORS[i % SUBJECT_COLORS.length]; });

  // Build time → day → entries map
  const allTimes = [...new Set(entries.map(e => {
    if (e.startDatetime) return new Date(e.startDatetime).toTimeString().slice(0,5);
    if (e.timeSlot?.startTime) return e.timeSlot.startTime.slice(0,5);
    return '09:00';
  }))].sort();

  return (
    <div>
      {selected && (
        <div className="modal-overlay" onClick={()=>setSelected(null)}>
          <div className="modal-box" style={{ maxWidth:'400px' }} onClick={e=>e.stopPropagation()}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'1rem' }}>
              <h3 style={{ margin:0 }}>{selected.title}</h3>
              <button onClick={()=>setSelected(null)} style={{ background:'none', border:'none', color:'var(--text-muted)', cursor:'pointer' }}><X size={18}/></button>
            </div>
            {[
              ['Day', selected.dayOfWeek],
              ['Type', selected.entryType],
              ['Status', selected.status],
              ['Instructor', selected.assignedTo?.fullName || '—'],
              ['Room', selected.resource?.name || '—'],
              ['Start', selected.startDatetime ? new Date(selected.startDatetime).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'}) : '—'],
              ['End', selected.endDatetime ? new Date(selected.endDatetime).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'}) : '—'],
              ['Recurring', selected.isRecurring ? 'Yes (Weekly)' : 'No'],
            ].map(([k,v])=>(
              <div key={k} style={{ display:'flex', justifyContent:'space-between', padding:'0.5rem 0', borderBottom:'1px solid var(--border-color)', fontSize:'0.875rem' }}>
                <span style={{ color:'var(--text-muted)' }}>{k}</span>
                <span style={{ color:'var(--text-primary)', fontWeight:500 }}>{v}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {allTimes.length === 0 ? (
        <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-muted)' }}>
          <Zap size={36} style={{ opacity:0.3, marginBottom:'1rem' }}/>
          <p>No schedule entries yet. Generate a timetable to populate this grid.</p>
        </div>
      ) : (
        <div style={{ overflowX:'auto' }}>
          <div className="tt-grid" style={{ gridTemplateColumns:`90px repeat(${activeDays.length},1fr)`, minWidth:`${90+activeDays.length*130}px` }}>
            <div className="tt-cell tt-header" style={{ minHeight:'44px' }}>Time</div>
            {activeDays.map(d=>(
              <div key={d} className="tt-cell tt-header" style={{ minHeight:'44px' }}>{DAY_LABELS[d]||d}</div>
            ))}
            {allTimes.map(time=>(
              <React.Fragment key={time}>
                <div className="tt-cell tt-header" style={{ fontSize:'0.72rem', minHeight:'70px', flexDirection:'column', gap:'2px' }}>
                  <Clock size={11}/>{time}
                </div>
                {activeDays.map(day=>{
                  const dayEntries = entries.filter(e=>{
                    const eDay = e.dayOfWeek;
                    const eTime = e.startDatetime
                      ? new Date(e.startDatetime).toTimeString().slice(0,5)
                      : (e.timeSlot?.startTime||'').slice(0,5);
                    return eDay===day && eTime===time;
                  });
                  return (
                    <div key={day} className="tt-cell" style={{ minHeight:'70px', padding:'0.35rem' }}>
                      {dayEntries.map((e,i)=>{
                        const col = colorMap[e.title] || SUBJECT_COLORS[0];
                        return (
                          <div key={i} onClick={()=>setSelected(e)} style={{
                            background:col.bg, border:`1px solid ${col.border}`, color:col.text,
                            borderRadius:'0.4rem', padding:'0.35rem 0.5rem', fontSize:'0.75rem',
                            fontWeight:600, cursor:'pointer', marginBottom:'0.25rem',
                            transition:'all 0.2s ease', lineHeight:1.3,
                          }}
                          onMouseEnter={ev=>ev.currentTarget.style.transform='translateY(-2px)'}
                          onMouseLeave={ev=>ev.currentTarget.style.transform=''}
                          title={`${e.title} | ${e.assignedTo?.fullName||''} | ${e.resource?.name||''}`}>
                            <div style={{ overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{e.title}</div>
                            {e.assignedTo?.fullName && <div style={{ fontSize:'0.68rem', opacity:0.75, marginTop:'2px' }}>👤 {e.assignedTo.fullName}</div>}
                            {e.resource?.name && <div style={{ fontSize:'0.68rem', opacity:0.75 }}>📍 {e.resource.name}</div>}
                          </div>
                        );
                      })}
                    </div>
                  );
                })}
              </React.Fragment>
            ))}
          </div>
        </div>
      )}

      {/* Legend */}
      {uniqueTitles.length > 0 && (
        <div style={{ display:'flex', flexWrap:'wrap', gap:'0.5rem', marginTop:'1rem' }}>
          {uniqueTitles.map((t,i)=>{
            const col = SUBJECT_COLORS[i%SUBJECT_COLORS.length];
            return (
              <div key={t} style={{ display:'flex', alignItems:'center', gap:'0.4rem', padding:'0.25rem 0.6rem', background:col.bg, border:`1px solid ${col.border}`, borderRadius:'2rem', fontSize:'0.72rem', color:col.text, fontWeight:600 }}>
                <div style={{ width:'8px', height:'8px', borderRadius:'50%', background:col.hex }}/>
                {t}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

/* ─── Main Component ─────────────────────────────────────── */
const Timetables = () => {
  const [timetables, setTimetables] = useState([]);
  const [selected, setSelected] = useState(null);
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [entriesLoading, setEntriesLoading] = useState(false);
  const [showWizard, setShowWizard] = useState(false);
  const [toast, setToast] = useState(null);
  const [teamMembers, setTeamMembers] = useState([]);
  const [resources, setResources] = useState([]);
  const [activeView, setActiveView] = useState('grid'); // 'grid' | 'list'
  const [activeDays, setActiveDays] = useState(['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY']);

  const showToast = (msg, type='success') => {
    setToast({ msg, type });
    setTimeout(()=>setToast(null), 4000);
  };

  const fetchTimetables = useCallback(async () => {
    try { setLoading(true);
      const res = await api.get('/api/v1/timetables');
      const list = res.data?.data?.content || res.data?.data || [];
      setTimetables(list);
    } catch { showToast('Failed to load timetables','error'); }
    finally { setLoading(false); }
  }, []);

  const fetchEntries = async (ttId) => {
    try { setEntriesLoading(true);
      const res = await api.get(`/api/v1/schedules/timetable/${ttId}`);
      setEntries(res.data?.data || []);
    } catch { setEntries([]); }
    finally { setEntriesLoading(false); }
  };

  const fetchSupportData = async () => {
    try {
      const [memRes, resRes] = await Promise.allSettled([
        api.get('/api/v1/users'),
        api.get('/api/v1/resources')
      ]);
      if (memRes.status==='fulfilled') setTeamMembers(memRes.value.data?.data?.content || memRes.value.data?.data || []);
      if (resRes.status==='fulfilled') setResources(resRes.value.data?.data?.content || resRes.value.data?.data || []);
    } catch {}
  };

  useEffect(() => { fetchTimetables(); fetchSupportData(); }, [fetchTimetables]);

  const handleSelect = async (tt) => { setSelected(tt); await fetchEntries(tt.id); };

  const handlePublish = async (id) => {
    try { await api.post(`/api/v1/timetables/${id}/publish`); showToast('Published!'); fetchTimetables(); setSelected(prev=>prev?.id===id?{...prev,status:'PUBLISHED'}:prev); }
    catch(e) { showToast(e.response?.data?.message||'Publish failed','error'); }
  };
  const handleArchive = async (id) => {
    try { await api.post(`/api/v1/timetables/${id}/archive`); showToast('Archived'); fetchTimetables(); if(selected?.id===id)setSelected(null); }
    catch { showToast('Archive failed','error'); }
  };
  const handleDelete = async (id) => {
    if(!window.confirm('Delete this timetable permanently?'))return;
    try { await api.delete(`/api/v1/timetables/${id}`); showToast('Deleted'); fetchTimetables(); if(selected?.id===id)setSelected(null); }
    catch(e) { showToast(e.response?.data?.message||'Delete failed','error'); }
  };
  const handleRegenerate = async () => {
    if(!selected)return;
    await fetchEntries(selected.id);
    showToast('Schedule refreshed');
  };
  const onWizardSuccess = (tt) => { setShowWizard(false); fetchTimetables(); handleSelect(tt); };

  const handleExportPDF = () => {
    window.print();
  };

  const handleExportCSV = () => {
    if (!entries || entries.length === 0) {
      showToast('No entries to export', 'error');
      return;
    }
    const headers = ['Day', 'Start Time', 'End Time', 'Subject', 'Teacher', 'Room'];
    const rows = entries.map(e => [
      e.dayOfWeek, e.startTime, e.endTime, e.subjectName, e.teacherName, e.roomName
    ].map(val => `"${val || ''}"`).join(','));
    const csvContent = "data:text/csv;charset=utf-8," + [headers.join(','), ...rows].join("\\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `timetable_${selected?.name || 'export'}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };
  const toggleDay = (d) => setActiveDays(prev => prev.includes(d) ? prev.filter(x=>x!==d) : [...prev,d]);

  return (
    <div style={{ display:'flex', height:'100%', overflow:'hidden' }}>
      {/* ─── Sidebar ─── */}
      <div style={{ width:'280px', borderRight:'1px solid var(--border-color)', display:'flex', flexDirection:'column', overflow:'hidden', flexShrink:0 }}>
        <div style={{ padding:'1.25rem', borderBottom:'1px solid var(--border-color)' }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'0.75rem' }}>
            <h2 style={{ fontSize:'1rem', fontWeight:700, margin:0 }}>Timetables</h2>
            <span style={{ fontSize:'0.72rem', color:'var(--text-muted)', background:'rgba(255,255,255,0.06)', padding:'0.15rem 0.5rem', borderRadius:'1rem' }}>{timetables.length}</span>
          </div>
          <button className="btn btn-primary btn-sm" onClick={()=>setShowWizard(true)} style={{ width:'100%', justifyContent:'center' }}>
            <Plus size={15}/> New Timetable
          </button>
        </div>

        <div style={{ overflowY:'auto', flex:1, padding:'0.75rem' }}>
          {loading ? (
            [1,2,3].map(i=><div key={i} className="skeleton" style={{ height:'72px', marginBottom:'0.65rem' }}/>)
          ) : timetables.length===0 ? (
            <div style={{ textAlign:'center', padding:'2.5rem 1rem', color:'var(--text-muted)' }}>
              <Calendar size={32} style={{ opacity:0.3, marginBottom:'0.75rem' }}/>
              <p style={{ fontSize:'0.82rem', margin:0 }}>No timetables yet.</p>
              <p style={{ fontSize:'0.82rem', margin:0 }}>Click <strong>New Timetable</strong> to start.</p>
            </div>
          ) : timetables.map(tt=>(
            <div key={tt.id} onClick={()=>handleSelect(tt)} style={{
              padding:'0.9rem', borderRadius:'var(--radius-sm)', cursor:'pointer', marginBottom:'0.5rem',
              background: selected?.id===tt.id ? 'rgba(79,70,229,0.1)':'rgba(255,255,255,0.02)',
              border:`1px solid ${selected?.id===tt.id?'rgba(79,70,229,0.35)':'var(--border-color)'}`,
              transition:'all 0.2s ease'
            }}>
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'0.4rem' }}>
                <div style={{ fontSize:'0.875rem', fontWeight:600, flex:1, marginRight:'0.5rem', lineHeight:1.3 }}>{tt.name}</div>
                <StatusBadge status={tt.status}/>
              </div>
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                <span style={{ fontSize:'0.72rem', color:'var(--text-muted)', display:'flex', alignItems:'center', gap:'0.3rem' }}>
                  <Clock size={10}/>{new Date(tt.createdAt).toLocaleDateString('en-IN')}
                </span>
                <button onClick={e=>{e.stopPropagation();handleDelete(tt.id);}} style={{ background:'none', border:'none', color:'var(--brand-danger)', cursor:'pointer', padding:'0.15rem', opacity:0.7 }}>
                  <Trash2 size={13}/>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ─── Main Area ─── */}
      <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>
        {!selected ? (
          <div style={{ flex:1, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:'1rem', color:'var(--text-muted)' }}>
            <div style={{ width:'100px', height:'100px', borderRadius:'50%', background:'rgba(79,70,229,0.08)', border:'1px solid rgba(79,70,229,0.2)', display:'flex', alignItems:'center', justifyContent:'center' }}>
              <Calendar size={44} color='rgba(79,70,229,0.5)'/>
            </div>
            <h3 style={{ margin:0, fontSize:'1.25rem' }}>Select a Timetable</h3>
            <p style={{ margin:0, textAlign:'center', maxWidth:'300px', fontSize:'0.875rem' }}>Choose a timetable from the sidebar to view its weekly schedule grid.</p>
            <button className="btn btn-primary" onClick={()=>setShowWizard(true)}>
              <Plus size={16}/> Create Your First Timetable
            </button>
          </div>
        ) : (
          <div style={{ flex:1, overflowY:'auto', padding:'1.5rem' }}>
            {/* Header */}
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'1.5rem', flexWrap:'wrap', gap:'0.75rem' }}>
              <div>
                <div style={{ display:'flex', alignItems:'center', gap:'0.75rem', marginBottom:'0.4rem' }}>
                  <h2 style={{ fontSize:'1.35rem', margin:0 }}>{selected.name}</h2>
                  <StatusBadge status={selected.status}/>
                </div>
                <p style={{ margin:0, fontSize:'0.82rem', color:'var(--text-muted)' }}>
                  {selected.effectiveFrom && `📅 ${selected.effectiveFrom} → ${selected.effectiveTo||'Ongoing'}`}
                  {' · '}{selected.timetableType||'WEEKLY'}
                </p>
              </div>
              <div className="no-print" style={{ display:'flex', gap:'0.4rem', flexWrap:'wrap' }}>
                <button className="glass-button btn-sm" onClick={handleExportPDF} style={{ fontSize:'0.78rem', background: 'rgba(79, 142, 247, 0.1)', color: 'var(--brand-primary)' }}>
                  <FileText size={13}/> PDF
                </button>
                <button className="glass-button btn-sm" onClick={handleExportCSV} style={{ fontSize:'0.78rem', background: 'rgba(16, 217, 160, 0.1)', color: 'var(--brand-success)' }}>
                  <Download size={13}/> CSV
                </button>
                <button className="glass-button btn-sm" onClick={handleRegenerate} style={{ fontSize:'0.78rem' }}>
                  <RefreshCw size={13}/> Refresh
                </button>
                {selected.status!=='PUBLISHED' && (
                  <button className="glass-button success btn-sm" onClick={()=>handlePublish(selected.id)} style={{ fontSize:'0.78rem' }}>
                    <CheckCircle size={13}/> Publish
                  </button>
                )}
                <button className="glass-button btn-sm" onClick={()=>handleArchive(selected.id)} style={{ fontSize:'0.78rem' }}>
                  <Archive size={13}/> Archive
                </button>
                <button className="glass-button danger btn-sm" onClick={()=>handleDelete(selected.id)} style={{ fontSize:'0.78rem' }}>
                  <Trash2 size={13}/> Delete
                </button>
                <button className="glass-button btn-sm" onClick={()=>setSelected(null)} style={{ fontSize:'0.78rem' }}>
                  <X size={13}/>
                </button>
              </div>
            </div>

            {/* Controls Row */}
            <div style={{ display:'flex', alignItems:'center', gap:'0.75rem', marginBottom:'1.25rem', flexWrap:'wrap' }}>
              <span style={{ fontSize:'0.8rem', color:'var(--text-muted)', fontWeight:600 }}>DAYS:</span>
              {DAYS.map(d=>(
                <button key={d} onClick={()=>toggleDay(d)} style={{
                  padding:'0.3rem 0.65rem', borderRadius:'2rem', fontSize:'0.75rem', fontWeight:600, cursor:'pointer',
                  border:`1px solid ${activeDays.includes(d)?'rgba(79,70,229,0.5)':'var(--border-color)'}`,
                  background: activeDays.includes(d)?'rgba(79,70,229,0.15)':'transparent',
                  color: activeDays.includes(d)?'#a5b4fc':'var(--text-muted)', transition:'all 0.2s'
                }}>{DAY_LABELS[d]}</button>
              ))}
              <div style={{ marginLeft:'auto', display:'flex', alignItems:'center', gap:'0.5rem' }}>
                <span style={{ fontSize:'0.75rem', color:'var(--text-muted)' }}>{entries.length} entries</span>
              </div>
            </div>

            {/* Grid Card */}
            <div className="glass-card" style={{ padding:'1.25rem' }}>
              <div style={{ display:'flex', alignItems:'center', gap:'0.5rem', marginBottom:'1rem' }}>
                <Calendar size={16} color='var(--brand-primary)'/>
                <h3 style={{ fontSize:'0.95rem', margin:0 }}>Weekly Schedule</h3>
                {entriesLoading && <span className="spinner" style={{ width:14,height:14, marginLeft:'0.5rem' }}/>}
              </div>
              <WeeklyGrid entries={entries} activeDays={activeDays}/>
            </div>

            {/* Time Slots Reference */}
            {selected.timeSlots?.length>0 && (
              <div className="glass-card" style={{ marginTop:'1.25rem', padding:'1.25rem' }}>
                <h3 style={{ fontSize:'0.9rem', margin:'0 0 0.9rem' }}>⏱ Time Slot Reference ({selected.timeSlots.length} slots)</h3>
                <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(190px,1fr))', gap:'0.6rem' }}>
                  {selected.timeSlots.map((ts,i)=>(
                    <div key={i} style={{ padding:'0.6rem 0.75rem', background:'rgba(255,255,255,0.03)', borderRadius:'var(--radius-sm)', border:'1px solid var(--border-color)', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                      <div>
                        <div style={{ fontSize:'0.8rem', fontWeight:600 }}>{ts.slotName}</div>
                        <div style={{ fontSize:'0.72rem', color:'var(--text-muted)' }}>{DAY_LABELS[ts.dayOfWeek]||ts.dayOfWeek}</div>
                      </div>
                      <div style={{ fontSize:'0.72rem', color:'var(--text-muted)', textAlign:'right' }}>
                        {ts.startTime?.slice(0,5)} – {ts.endTime?.slice(0,5)}
                        <div style={{ fontSize:'0.68rem', opacity:0.7 }}>{ts.slotType}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Wizard */}
      {showWizard && (
        <CreateWizard onClose={()=>setShowWizard(false)} onSuccess={onWizardSuccess}
          teamMembers={teamMembers} resources={resources} showToast={showToast}/>
      )}

      <Toast toast={toast}/>
    </div>
  );
};

export default Timetables;
