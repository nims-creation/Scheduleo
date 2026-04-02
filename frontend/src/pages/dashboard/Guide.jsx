import React, { useState } from 'react';
import { BookOpen, Calendar, Clock, Users, CreditCard, LayoutDashboard, Settings, Info, ChevronRight, Zap, ShieldCheck } from 'lucide-react';

const GUIDES = [
  {
    id: 'getting-started',
    title: 'Welcome & Getting Started',
    icon: LayoutDashboard,
    content: (
      <div className="guide-content">
        <h2 style={{ fontSize: '1.8rem', fontWeight: 800, marginBottom: '1rem', color: 'var(--text-primary)' }}>Welcome to Schedulo!</h2>
        <p>Schedulo is a premium SaaS platform designed to automate and simplify resource scheduling for organizations, schools, and hospitals.</p>
        
        <h3>The Dashboard Overview</h3>
        <p>When you log in, you are greeted by the Dashboard Home. This gives you a high-level view of your current key performance indicators (KPIs), upcoming tasks, and quick actions.</p>
        
        <div style={{ background: 'rgba(79, 142, 247, 0.08)', borderLeft: '4px solid var(--brand-primary)', padding: '1rem', borderRadius: '4px', margin: '1.5rem 0' }}>
          <strong style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}><Info size={18}/> Quick Tip</strong>
          Use the left sidebar to navigate across the platform. The sidebar displays sections organized logically by <strong>Overview</strong>, <strong>Organization</strong>, and <strong>Insights</strong>.
        </div>
      </div>
    )
  },
  {
    id: 'timetables',
    title: 'Timetables & AI Generation',
    icon: Clock,
    content: (
      <div className="guide-content">
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1rem' }}>Automated Timetable Generation</h2>
        <p>Schedulo's core feature is its conflict-free timetable generation engine, taking the manual labor out of scheduling.</p>
        
        <ol style={{ paddingLeft: '1.5rem', marginTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <li>
            <strong>Launch Wizard:</strong> Navigate to the <strong>Timetables</strong> page and click <span style={{ background: 'var(--brand-primary)', color:'white', padding:'2px 8px', borderRadius:'12px', fontSize:'0.8rem'}}>+ New Timetable</span>.
          </li>
          <li>
            <strong>Step 1 - Basic Info:</strong> Give your timetable a descriptive name, select the operational date range, and ensure the format (e.g. Weekly) is correct.
          </li>
          <li>
            <strong>Step 2 - Time Slots:</strong> Define the structure of your day. Add slots like "Period 1" (08:00 - 09:00) and "Lunch Break". Click <em>Apply Mon→Fri</em> to rapidly copy these core slots to the whole week.
          </li>
          <li>
            <strong>Step 3 - Subjects & Classes:</strong> Add the subjects/requirements. Assign a Teacher and a Room. Set how many periods per week the subject requires.
          </li>
          <li>
            <strong>Step 4 - AI Generation:</strong> Review the summary and hit <strong>Generate</strong>. Schedulo will search for conflicts and output the most optimal grid.
          </li>
        </ol>

        <div style={{ background: 'rgba(16, 217, 160, 0.08)', border: '1px solid rgba(16, 217, 160, 0.3)', padding: '1rem', borderRadius: '8px', margin: '1.5rem 0' }}>
          <strong style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--brand-success)' }}><Zap size={18}/> Zero Conflicts Guaranteed</strong>
          <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.9rem' }}>The generator employs a backtracking algorithm to guarantee that no teacher or room is double-booked across the entire schedule.</p>
        </div>
      </div>
    )
  },
  {
    id: 'team-members',
    title: 'Team Members & Bulk Import',
    icon: Users,
    content: (
      <div className="guide-content">
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1rem' }}>Managing Your Organization Flow</h2>
        <p>Under the <strong>Team Members</strong> tab, you can view all active users associated with your organizational instance.</p>
        
        <h3>Inviting Members</h3>
        <p>Click the <strong>Invite Member</strong> button to send single-user invitations. You can assign them roles like <code>ADMIN</code>, <code>MANAGER</code>, or standard <code>USER</code>.</p>
        
        <h3>Bulk CSV Upload</h3>
        <p>If you have hundreds of students or staff members:</p>
        <ul style={{ paddingLeft: '1.5rem', marginTop: '0.5rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <li>Click <strong>Bulk Import</strong>.</li>
          <li>Use the <strong>Upload CSV</strong> button inside the modal to select your spreadsheet.</li>
          <li>The system expects a format like: <code>Email, FirstName, LastName</code>.</li>
          <li>Imported users immediately become active and can instantly be assigned to timetable subjects!</li>
        </ul>
      </div>
    )
  },
  {
    id: 'calendar',
    title: 'Calendar & Events',
    icon: Calendar,
    content: (
      <div className="guide-content">
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1rem' }}>Organization Calendar</h2>
        <p>The Calendar gives you an aggregated view of the entire organization's lifecycle mapping.</p>
        
        <ul style={{ paddingLeft: '1.5rem', marginTop: '1rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <li><strong>Daily/Monthly Views:</strong> Switch views to drill down or get a bird's eye look.</li>
          <li><strong>Add Holidays:</strong> Mark particular days as holidays. Our timetable engine automatically avoids assigning classes on designated organization-wide holidays!</li>
          <li><strong>Custom Events:</strong> Add organizational events like "Staff Meeting" so they are visible to everyone on the dashboard.</li>
        </ul>
      </div>
    )
  },
  {
    id: 'billing',
    title: 'Billing & Subscriptions',
    icon: CreditCard,
    content: (
      <div className="guide-content">
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1rem' }}>Subscription Processing</h2>
        <p>Scale Schedulo alongside your organizational expansion easily from the <strong>Billing</strong> page.</p>

        <h3>Changing Currency</h3>
        <p>At the very top of the Billing screen, select your preferred local currency (e.g., INR, USD, EUR, GBP). Prices instantly adjust to match live exchange parameters.</p>

        <h3>Upgrading Plans</h3>
        <p>We process payments through a secure, simulated Stripe gateway. Click <strong>Select Plan</strong>, enter card details in the secure pop-up (use dummy strings like `4242 4242...`), and confirm. You’ll be instantly upgraded to higher capacity generations and limits!</p>
        
        <div style={{ background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239, 68, 68, 0.3)', padding: '1rem', borderRadius: '8px', margin: '1.5rem 0' }}>
          <strong style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--brand-danger)' }}><ShieldCheck size={18}/> Secure Processing</strong>
          <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.9rem' }}>Schedulo ensures that sensitive cardholder data is tokenized securely before ever reaching our backend API schemas.</p>
        </div>
      </div>
    )
  }
];

const Guide = () => {
  const [activeTab, setActiveTab] = useState(GUIDES[0].id);

  const activeContent = GUIDES.find(g => g.id === activeTab);

  return (
    <div style={{ padding: '2.5rem', display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
      
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, margin: '0 0 0.5rem 0', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <BookOpen size={32} color="var(--brand-primary)" />
          System Documentation
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', margin: 0 }}>
          Everything you need to know about setting up and dominating your schedules.
        </p>
      </div>

      <div style={{ display: 'flex', flex: 1, gap: '2.5rem', minHeight: 0 }}>
        {/* Left Sidebar Menu */}
        <div style={{ width: '280px', flexShrink: 0, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.1em', paddingBottom: '0.5rem', marginBottom: '0.5rem', borderBottom: '1px solid var(--border-color)' }}>
            Contents
          </div>
          {GUIDES.map(guide => {
            const Icon = guide.icon;
            const isActive = activeTab === guide.id;
            return (
              <button
                key={guide.id}
                onClick={() => setActiveTab(guide.id)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '1rem', cursor: 'pointer', textAlign: 'left',
                  borderRadius: '12px', border: isActive ? '1px solid rgba(79, 142, 247, 0.4)' : '1px solid transparent',
                  background: isActive ? 'rgba(79, 142, 247, 0.08)' : 'var(--bg-secondary)',
                  color: isActive ? 'var(--brand-primary)' : 'var(--text-primary)',
                  transition: 'all 0.2s', fontWeight: isActive ? 600 : 500
                }}
              >
                <div style={{ 
                  background: isActive ? 'var(--brand-gradient)' : 'rgba(255,255,255,0.05)', 
                  color: isActive ? 'white' : 'var(--text-muted)', 
                  padding: '8px', borderRadius: '8px', display: 'flex'
                }}>
                  <Icon size={18} />
                </div>
                <span style={{ flex: 1 }}>{guide.title}</span>
                {isActive && <ChevronRight size={16} />}
              </button>
            )
          })}
        </div>

        {/* Content Viewer pane */}
        <div className="glass-card" style={{ flex: 1, padding: '3rem', overflowY: 'auto', borderRadius: '16px' }}>
          {activeContent && (
            <div className="fade-in">
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem', paddingBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
                <div style={{ background: 'var(--brand-gradient)', padding: '12px', borderRadius: '12px', color: 'white' }}>
                  <activeContent.icon size={28} />
                </div>
                <h1 style={{ fontSize: '2.2rem', fontWeight: 800, margin: 0 }}>{activeContent.title}</h1>
              </div>
              
              <div style={{ fontSize: '1.05rem', lineHeight: 1.6, color: 'var(--text-secondary)' }}>
                {activeContent.content}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Guide;
