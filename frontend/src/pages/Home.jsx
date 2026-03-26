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
      <main className="container animate-slide-up" style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '6rem 1.5rem' }}>
        <h1 style={{ fontSize: '3.5rem', marginBottom: '1.5rem', maxWidth: '800px' }}>
          Master your time with <span className="text-gradient">precision</span> and <span className="text-gradient">ease</span>
        </h1>
        <p style={{ fontSize: '1.25rem', color: 'var(--text-secondary)', maxWidth: '600px', marginBottom: '3rem' }}>
          Schedulo is your all-in-one platform for intelligent scheduling, team timetables, and automated conflict resolution.
        </p>
        
        <div style={{ display: 'flex', gap: '1.5rem' }}>
           <Link to="/signup" className="btn btn-primary" style={{ padding: '1rem 2rem', fontSize: '1.1rem' }}>
             Start for free <ArrowRight size={20} />
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
