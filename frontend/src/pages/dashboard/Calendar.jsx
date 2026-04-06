import React, { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, Plus, Calendar as CalendarIcon, MapPin, Video, Clock } from 'lucide-react';
import api from '../../services/api';

const Calendar = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [events, setEvents] = useState([]);
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Modals state
  const [showEventModal, setShowEventModal] = useState(false);
  const [showHolidayModal, setShowHolidayModal] = useState(false);

  // Form states
  const [eventForm, setEventForm] = useState({
    title: '', description: '', startDate: '', startTime: '', endDate: '', endTime: '',
    eventType: 'MEETING', location: '', virtualMeetingUrl: ''
  });
  
  const [holidayForm, setHolidayForm] = useState({
    name: '', description: '', holidayDate: '', holidayType: 'PUBLIC', isHalfDay: false
  });

  const getMonthStartAndEnd = (date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    // Fetch a bit wider range to cover the whole grid
    const start = new Date(year, month - 1, 20).toISOString();
    const end = new Date(year, month + 1, 10).toISOString();
    
    const startOnlyDate = new Date(year, month - 1, 20).toISOString().split('T')[0];
    const endOnlyDate = new Date(year, month + 1, 10).toISOString().split('T')[0];
    
    return { start, end, startOnlyDate, endOnlyDate };
  };

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const { start, end, startOnlyDate, endOnlyDate } = getMonthStartAndEnd(currentDate);

      const [eventsRes, holidaysRes] = await Promise.all([
        api.get('/api/v1/calendar/events', { params: { start, end } }),
        api.get('/api/v1/calendar/holidays', { params: { start: startOnlyDate, end: endOnlyDate } })
      ]);

      setEvents(eventsRes.data.data || []);
      setHolidays(holidaysRes.data.data || []);
    } catch (err) {
      console.error(err);
      setError('Failed to load calendar data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [currentDate]);

  const prevMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  const nextMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
  const today = () => setCurrentDate(new Date());

  // Calendar Grid Logic
  const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate();
  const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).getDay();

  const days = [];
  // previous month days
  for (let i = 0; i < firstDayOfMonth; i++) {
    const d = new Date(currentDate.getFullYear(), currentDate.getMonth(), 0 - i);
    days.unshift({ date: d, isCurrentMonth: false });
  }
  // current month days
  for (let i = 1; i <= daysInMonth; i++) {
    days.push({ date: new Date(currentDate.getFullYear(), currentDate.getMonth(), i), isCurrentMonth: true });
  }
  // next month days to complete grid (42 cells to ensure 6 rows)
  const remainingCells = 42 - days.length;
  for (let i = 1; i <= remainingCells; i++) {
    days.push({ date: new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, i), isCurrentMonth: false });
  }

  const isToday = (date) => {
    const today = new Date();
    return date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear();
  };

  const getEventsForDate = (date) => {
    const dateStr = date.toISOString().split('T')[0];
    const dayEvents = events.filter(e => e.startDatetime.startsWith(dateStr));
    const dayHolidays = holidays.filter(h => h.holidayDate === dateStr);
    return { dayEvents, dayHolidays };
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    try {
      const startDatetime = `${eventForm.startDate}T${eventForm.startTime || '00:00'}:00`;
      let endDatetime = `${eventForm.endDate}T${eventForm.endTime || '23:59'}:00`;
      
      if (!eventForm.endDate) {
         endDatetime = `${eventForm.startDate}T${eventForm.endTime || '23:59'}:00`;
      }

      await api.post('/api/v1/calendar/events', {
        title: eventForm.title,
        description: eventForm.description,
        startDatetime,
        endDatetime,
        eventType: eventForm.eventType,
        location: eventForm.location,
        virtualMeetingUrl: eventForm.virtualMeetingUrl
      });
      setShowEventModal(false);
      setEventForm({ title: '', description: '', startDate: '', startTime: '', endDate: '', endTime: '', eventType: 'MEETING', location: '', virtualMeetingUrl: ''});
      fetchData();
    } catch (err) {
      let errorText = err.response?.data?.message || err.message;
      if (err.response?.data?.error?.details) {
        const details = err.response.data.error.details;
        if (typeof details === 'object' && Object.keys(details).length > 0) {
          errorText = Object.values(details)[0];
        }
      }
      setError('Error creating event: ' + errorText);
    }
  };

  const handleCreateHoliday = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/v1/calendar/holidays', {
        name: holidayForm.name,
        description: holidayForm.description,
        holidayDate: holidayForm.holidayDate,
        holidayType: holidayForm.holidayType,
        isHalfDay: holidayForm.isHalfDay
      });
      setShowHolidayModal(false);
      setHolidayForm({ name: '', description: '', holidayDate: '', holidayType: 'PUBLIC_HOLIDAY', isHalfDay: false });
      fetchData();
    } catch (err) {
      let errorText = err.response?.data?.message || err.message;
      if (err.response?.data?.error?.details) {
        const details = err.response.data.error.details;
        if (typeof details === 'object' && Object.keys(details).length > 0) {
          errorText = Object.values(details)[0];
        }
      }
      setError('Error creating holiday: ' + errorText);
    }
  };

  const openDateModal = (dateObj) => {
    setError(null);
    const dStr = new Date(dateObj.date.getTime() - dateObj.date.getTimezoneOffset() * 60000).toISOString().split('T')[0];
    setSelectedDate(dStr);
    setEventForm({ ...eventForm, startDate: dStr, endDate: dStr });
    setHolidayForm({ ...holidayForm, holidayDate: dStr });
    setShowEventModal(true);
  };

  return (
    <div className="page-wrapper" style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.5rem 0', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <CalendarIcon size={28} color="var(--brand-primary)" />
            Organization Calendar
          </h1>
          <p style={{ color: 'var(--text-muted)', margin: 0, fontSize: '0.875rem' }}>Manage events, meetings, and holidays.</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className="btn btn-outline" onClick={() => { setError(null); setShowHolidayModal(true); }}>
            <Plus size={16} /> Add Holiday
          </button>
          <button className="btn btn-primary" onClick={() => { setError(null); setShowEventModal(true); }}>
            <Plus size={16} /> Add Event
          </button>
        </div>
      </div>

      <div style={{ background: 'var(--bg-secondary)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', overflow: 'hidden' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1.5rem', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>
            {currentDate.toLocaleString('default', { month: 'long', year: 'numeric' })}
          </div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn btn-outline" style={{ padding: '0.5rem' }} onClick={prevMonth}>
              <ChevronLeft size={18} />
            </button>
            <button className="btn btn-outline" onClick={today}>Today</button>
            <button className="btn btn-outline" style={{ padding: '0.5rem' }} onClick={nextMonth}>
              <ChevronRight size={18} />
            </button>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', textAlign: 'center', background: 'rgba(0,0,0,0.2)', borderBottom: '1px solid var(--border-color)' }}>
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
            <div key={day} style={{ padding: '1rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>{day}</div>
          ))}
        </div>

        {loading ? (
            <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>Loading calendar...</div>
        ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', autoRows: 'minmax(120px, auto)' }}>
            {days.map((dayObj, i) => {
                const { dayEvents, dayHolidays } = getEventsForDate(dayObj.date);
                
                return (
                <div 
                    key={i} 
                    onClick={() => openDateModal(dayObj)}
                    style={{ 
                    borderRight: '1px solid var(--border-color)', 
                    borderBottom: '1px solid var(--border-color)',
                    padding: '0.5rem',
                    background: dayObj.isCurrentMonth ? (isToday(dayObj.date) ? 'rgba(79, 142, 247, 0.05)' : 'transparent') : 'rgba(0,0,0,0.1)',
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                    }}
                    onMouseEnter={e => {
                        if(!isToday(dayObj.date)) e.currentTarget.style.background = 'var(--bg-tertiary)';
                    }}
                    onMouseLeave={e => {
                        e.currentTarget.style.background = dayObj.isCurrentMonth ? (isToday(dayObj.date) ? 'rgba(79, 142, 247, 0.05)' : 'transparent') : 'rgba(0,0,0,0.1)';
                    }}
                >
                    <div style={{ 
                        display: 'flex', justifyContent: 'center', alignItems: 'center',
                        width: '28px', height: '28px', borderRadius: '50%',
                        margin: '0 auto 0.5rem auto',
                        background: isToday(dayObj.date) ? 'var(--brand-primary)' : 'transparent',
                        color: isToday(dayObj.date) ? 'white' : (dayObj.isCurrentMonth ? 'var(--text-primary)' : 'var(--text-muted)'),
                        fontWeight: isToday(dayObj.date) ? 600 : 400,
                        fontSize: '0.875rem'
                    }}>
                    {dayObj.date.getDate()}
                    </div>
                    
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                    {dayHolidays.map((h, idx) => (
                        <div key={`h-${idx}`} style={{ 
                        fontSize: '0.7rem', padding: '0.25rem 0.5rem', borderRadius: '4px',
                        background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)',
                        whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'
                        }}>
                        🌴 {h.name}
                        </div>
                    ))}
                    {dayEvents.map((e, idx) => (
                        <div key={`e-${idx}`} style={{ 
                        fontSize: '0.7rem', padding: '0.25rem 0.5rem', borderRadius: '4px',
                        background: 'rgba(79, 142, 247, 0.1)', color: 'var(--brand-primary)', border: '1px solid rgba(79, 142, 247, 0.2)',
                        whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'
                        }}>
                        {e.startDatetime.split('T')[1].substring(0, 5)} {e.title}
                        </div>
                    ))}
                    </div>
                </div>
                );
            })}
            </div>
        )}
      </div>

      {/* Event Modal */}
      {showEventModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(6px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '520px', padding: '2rem', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>Create Event</h3>
                <p style={{ margin: '0.25rem 0 0', color: 'var(--text-muted)', fontSize: '0.8rem' }}>Add a meeting, class, or exam to the calendar</p>
              </div>
              <button
                onClick={() => setShowEventModal(false)}
                style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid var(--border-color)', color: 'var(--text-muted)', borderRadius: '8px', width: '32px', height: '32px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.1rem', transition: 'all 0.2s' }}
                onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.12)'}
                onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.07)'}
              >×</button>
            </div>

            {error && (
              <div style={{ background: 'rgba(2ef4444, 0.1)', borderLeft: '4px solid var(--brand-danger)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', color: 'var(--brand-danger)', fontSize: '0.9rem' }}>
                {error}
              </div>
            )}

            <form onSubmit={handleCreateEvent}>
              <div className="input-group">
                <label className="input-label">Event Title</label>
                <input type="text" className="input-field" required placeholder="e.g. Team Standup, Physics Class" value={eventForm.title} onChange={e => setEventForm({...eventForm, title: e.target.value})} />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="input-group">
                  <label className="input-label">Start Date</label>
                  <input type="date" className="input-field" required value={eventForm.startDate} onChange={e => setEventForm({...eventForm, startDate: e.target.value})} />
                </div>
                <div className="input-group">
                  <label className="input-label">Start Time</label>
                  <input type="time" className="input-field" value={eventForm.startTime} onChange={e => setEventForm({...eventForm, startTime: e.target.value})} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="input-group">
                  <label className="input-label">End Date <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>(optional)</span></label>
                  <input type="date" className="input-field" value={eventForm.endDate} onChange={e => setEventForm({...eventForm, endDate: e.target.value})} />
                </div>
                <div className="input-group">
                  <label className="input-label">End Time</label>
                  <input type="time" className="input-field" value={eventForm.endTime} onChange={e => setEventForm({...eventForm, endTime: e.target.value})} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="input-group">
                  <label className="input-label">Event Type</label>
                  <select className="input-field" value={eventForm.eventType} onChange={e => setEventForm({...eventForm, eventType: e.target.value})} style={{ cursor: 'pointer' }}>
                    <option value="MEETING">📅 Meeting</option>
                    <option value="EXAM">📝 Exam</option>
                    <option value="CLASS">📚 Class</option>
                    <option value="OTHER">🔖 Other</option>
                  </select>
                </div>
                <div className="input-group">
                  <label className="input-label">Location</label>
                  <input type="text" className="input-field" placeholder="Room 101" value={eventForm.location} onChange={e => setEventForm({...eventForm, location: e.target.value})} />
                </div>
              </div>

              <div className="input-group">
                <label className="input-label">Description <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>(optional)</span></label>
                <textarea className="input-field" rows="2" value={eventForm.description} onChange={e => setEventForm({...eventForm, description: e.target.value})} style={{ resize: 'vertical' }}></textarea>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.5rem' }}>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowEventModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>📅 Save Event</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Holiday Modal */}
      {showHolidayModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(6px)' }}>
          <div className="glass-panel animate-slide-up" style={{ width: '100%', maxWidth: '460px', padding: '2rem', position: 'relative' }}>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>Add Holiday</h3>
                <p style={{ margin: '0.25rem 0 0', color: 'var(--text-muted)', fontSize: '0.8rem' }}>Mark a date as a non-working day</p>
              </div>
              <button
                onClick={() => setShowHolidayModal(false)}
                style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid var(--border-color)', color: 'var(--text-muted)', borderRadius: '8px', width: '32px', height: '32px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.1rem', transition: 'all 0.2s' }}
                onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.12)'}
                onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.07)'}
              >×</button>
            </div>

            {error && (
              <div style={{ background: 'rgba(2ef4444, 0.1)', borderLeft: '4px solid var(--brand-danger)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', color: 'var(--brand-danger)', fontSize: '0.9rem' }}>
                {error}
              </div>
            )}

            <form onSubmit={handleCreateHoliday}>
              <div className="input-group">
                <label className="input-label">Holiday Name</label>
                <input
                  type="text"
                  className="input-field"
                  required
                  placeholder="e.g. Independence Day"
                  value={holidayForm.name}
                  onChange={e => setHolidayForm({...holidayForm, name: e.target.value})}
                />
              </div>

              <div className="input-group">
                <label className="input-label">Date</label>
                <input
                  type="date"
                  className="input-field"
                  required
                  value={holidayForm.holidayDate}
                  onChange={e => setHolidayForm({...holidayForm, holidayDate: e.target.value})}
                />
              </div>

              <div className="input-group">
                <label className="input-label">Holiday Type</label>
                <select
                  className="input-field"
                  value={holidayForm.holidayType}
                  onChange={e => setHolidayForm({...holidayForm, holidayType: e.target.value})}
                  style={{ cursor: 'pointer' }}
                >
                  <option value="PUBLIC_HOLIDAY">🌐 Public</option>
                  <option value="COMPANY_HOLIDAY">🏢 Organizational</option>
                  <option value="OTHER">🕌 Religious</option>
                  <option value="OPTIONAL">📋 Optional</option>
                </select>
              </div>

              <div className="input-group">
                <label className="input-label">Description (Optional)</label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Brief note about this holiday"
                  value={holidayForm.description}
                  onChange={e => setHolidayForm({...holidayForm, description: e.target.value})}
                />
              </div>

              {/* Half-day toggle */}
              <div
                onClick={() => setHolidayForm({...holidayForm, isHalfDay: !holidayForm.isHalfDay})}
                style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '0.875rem 1rem', borderRadius: 'var(--radius-sm)',
                  border: `1px solid ${holidayForm.isHalfDay ? 'var(--brand-primary)' : 'var(--border-color)'}`,
                  background: holidayForm.isHalfDay ? 'rgba(16, 217, 160, 0.05)' : 'rgba(255,255,255,0.03)',
                  cursor: 'pointer', marginBottom: '1.5rem', transition: 'all 0.2s'
                }}
              >
                <div>
                  <div style={{ fontWeight: 500, fontSize: '0.9rem' }}>Half Day</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.78rem' }}>Only half the day is off</div>
                </div>
                <div style={{
                  width: '42px', height: '24px', borderRadius: '12px', position: 'relative', transition: 'all 0.2s',
                  background: holidayForm.isHalfDay ? 'var(--brand-primary)' : 'rgba(255,255,255,0.1)'
                }}>
                  <div style={{
                    position: 'absolute', top: '3px', left: holidayForm.isHalfDay ? '21px' : '3px',
                    width: '18px', height: '18px', borderRadius: '50%', background: 'white', transition: 'all 0.2s',
                    boxShadow: '0 1px 4px rgba(0,0,0,0.3)'
                  }} />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowHolidayModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1, background: 'linear-gradient(135deg, #ef4444, #dc2626)' }}>
                  🌴 Save Holiday
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Calendar;

