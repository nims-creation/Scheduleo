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
  const [selectedDate, setSelectedDate] = useState(null);

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
        api.get('/calendar/events', { params: { start, end } }),
        api.get('/calendar/holidays', { params: { start: startOnlyDate, end: endOnlyDate } })
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

      await api.post('/calendar/events', {
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
      alert('Error creating event: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleCreateHoliday = async (e) => {
    e.preventDefault();
    try {
      await api.post('/calendar/holidays', {
        name: holidayForm.name,
        description: holidayForm.description,
        holidayDate: holidayForm.holidayDate,
        holidayType: holidayForm.holidayType,
        isHalfDay: holidayForm.isHalfDay
      });
      setShowHolidayModal(false);
      setHolidayForm({ name: '', description: '', holidayDate: '', holidayType: 'PUBLIC', isHalfDay: false });
      fetchData();
    } catch (err) {
      alert('Error creating holiday: ' + (err.response?.data?.message || err.message));
    }
  };

  const openDateModal = (dateObj) => {
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
          <button className="btn btn-outline" onClick={() => setShowHolidayModal(true)}>
            <Plus size={16} /> Add Holiday
          </button>
          <button className="btn btn-primary" onClick={() => setShowEventModal(true)}>
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
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div style={{ background: 'var(--bg-primary)', padding: '2rem', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '500px', border: '1px solid var(--border-color)', boxShadow: '0 20px 40px rgba(0,0,0,0.2)' }}>
            <h3 style={{ margin: '0 0 1.5rem', display: 'flex', justifyContent: 'space-between' }}>
              Create Event
              <button onClick={() => setShowEventModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '1.2rem' }}>×</button>
            </h3>
            <form onSubmit={handleCreateEvent} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label>Event Title</label>
                <input type="text" className="form-control" required value={eventForm.title} onChange={e => setEventForm({...eventForm, title: e.target.value})} placeholder="Project Sync" />
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Start Date</label>
                  <input type="date" className="form-control" required value={eventForm.startDate} onChange={e => setEventForm({...eventForm, startDate: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Start Time</label>
                  <input type="time" className="form-control" value={eventForm.startTime} onChange={e => setEventForm({...eventForm, startTime: e.target.value})} />
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>End Date (Optional)</label>
                  <input type="date" className="form-control" value={eventForm.endDate} onChange={e => setEventForm({...eventForm, endDate: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>End Time</label>
                  <input type="time" className="form-control" value={eventForm.endTime} onChange={e => setEventForm({...eventForm, endTime: e.target.value})} />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Event Type</label>
                    <select className="form-control" value={eventForm.eventType} onChange={e => setEventForm({...eventForm, eventType: e.target.value})}>
                        <option value="MEETING">Meeting</option>
                        <option value="EXAM">Exam</option>
                        <option value="CLASS">Class</option>
                        <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Location</label>
                    <input type="text" className="form-control" value={eventForm.location} onChange={e => setEventForm({...eventForm, location: e.target.value})} placeholder="Room 101" />
                  </div>
              </div>

              <div className="form-group">
                <label>Description (Optional)</label>
                <textarea className="form-control" rows="2" value={eventForm.description} onChange={e => setEventForm({...eventForm, description: e.target.value})}></textarea>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" className="btn btn-outline" onClick={() => setShowEventModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Event</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Holiday Modal */}
      {showHolidayModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div style={{ background: 'var(--bg-primary)', padding: '2rem', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '400px', border: '1px solid var(--border-color)', boxShadow: '0 20px 40px rgba(0,0,0,0.2)' }}>
            <h3 style={{ margin: '0 0 1.5rem', display: 'flex', justifyContent: 'space-between' }}>
              Add Holiday
              <button onClick={() => setShowHolidayModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '1.2rem' }}>×</button>
            </h3>
            <form onSubmit={handleCreateHoliday} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label>Holiday Name</label>
                <input type="text" className="form-control" required value={holidayForm.name} onChange={e => setHolidayForm({...holidayForm, name: e.target.value})} placeholder="New Year's Day" />
              </div>
              
              <div className="form-group">
                <label>Date</label>
                <input type="date" className="form-control" required value={holidayForm.holidayDate} onChange={e => setHolidayForm({...holidayForm, holidayDate: e.target.value})} />
              </div>
              
              <div className="form-group">
                <label>Type</label>
                <select className="form-control" value={holidayForm.holidayType} onChange={e => setHolidayForm({...holidayForm, holidayType: e.target.value})}>
                  <option value="PUBLIC">Public</option>
                  <option value="ORGANIZATIONAL">Organizational</option>
                  <option value="RELIGIOUS">Religious</option>
                  <option value="OPTIONAL">Optional</option>
                </select>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
                <input type="checkbox" id="halfDay" checked={holidayForm.isHalfDay} onChange={e => setHolidayForm({...holidayForm, isHalfDay: e.target.checked})} />
                <label htmlFor="halfDay" style={{ margin: 0 }}>Half Day</label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-outline" onClick={() => setShowHolidayModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" style={{ background: '#ef4444', borderColor: '#ef4444' }}>Save Holiday</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Calendar;
