import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, Users, ArrowRight } from 'lucide-react';

const Home = () => {
  return (
    <div className="page-wrapper" style={{ background: 'var(--bg-primary)' }}>
      {/* Navigation */}
      <nav style={{ padding: '1.5rem', borderBottom: '1px solid var(--border-color)' }}>
        <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ background: 'var(--brand-gradient)', padding: '0.5rem', borderRadius: 'var(--radius-sm)' }}>
              <Calendar color="white" size={24} />
            </div>
            <span style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Schedulo</span>
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <Link to="/login" className="btn btn-secondary">Login</Link>
            <Link to="/signup" className="btn btn-primary">Get Started</Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="container animate-slide-up" style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '6rem 1.5rem', position: 'relative', zIndex: 10 }}>
        
        <div style={{ marginBottom: '2rem', padding: '0.4rem 1.25rem', background: 'rgba(59,130,246,0.1)', border: '1px solid rgba(59,130,246,0.2)', borderRadius: '2rem', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', color: 'var(--brand-primary)', fontSize: '0.875rem', fontWeight: '500', boxShadow: '0 4px 20px rgba(59,130,246,0.1)' }}>
          <span style={{ display: 'inline-block', width: '8px', height: '8px', background: 'var(--brand-primary)', borderRadius: '50%', boxShadow: '0 0 10px var(--brand-primary)', animation: 'pulse 2s infinite' }}></span>
          Schedulo 2.0 is now live
        </div>

        <h1 style={{ fontSize: '4.5rem', marginBottom: '1.5rem', maxWidth: '900px', letterSpacing: '-0.02em', lineHeight: '1.1' }}>
          Master your time with <span className="text-gradient">precision</span> and <span className="text-gradient">ease</span>
        </h1>
        <p style={{ fontSize: '1.25rem', color: 'var(--text-secondary)', maxWidth: '600px', marginBottom: '3rem', lineHeight: '1.6' }}>
          Schedulo is your all-in-one platform for intelligent scheduling, team timetables, and automated conflict resolution.
        </p>
        
        <div style={{ display: 'flex', gap: '1.5rem' }}>
           <Link to="/signup" className="btn btn-primary" style={{ padding: '1.25rem 2.5rem', fontSize: '1.1rem', borderRadius: '3rem' }}>
             Start for free <ArrowRight size={20} />
           </Link>
           <Link to="/login" className="btn btn-secondary" style={{ padding: '1.25rem 2.5rem', fontSize: '1.1rem', borderRadius: '3rem', background: 'rgba(255,255,255,0.03)' }}>
             Book Demo
           </Link>
        </div>

        {/* Feature Cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem', marginTop: '5rem', width: '100%', textAlign: 'left' }}>
          <div className="glass-card">
            <Clock color="var(--brand-primary)" size={32} style={{ marginBottom: '1rem' }} />
            <h3>Automated Scheduling</h3>
            <p>Our intelligent engine instantly flags conflicts and suggests optimal time slots for your entire team.</p>
          </div>
          <div className="glass-card">
            <Users color="var(--brand-secondary)" size={32} style={{ marginBottom: '1rem' }} />
            <h3>Organization Sync</h3>
            <p>Manage multiple departments, branches, and robust permission boundaries seamlessly.</p>
          </div>
          <div className="glass-card">
            <Calendar color="var(--brand-accent)" size={32} style={{ marginBottom: '1rem' }} />
            <h3>Dynamic Timetables</h3>
            <p>Export, share, and visualize recurring schedule patterns instantly through our modern interface.</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Home;
