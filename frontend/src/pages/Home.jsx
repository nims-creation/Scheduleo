import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, Users, ArrowRight, Zap, Shield, BarChart3, CheckCircle, Star } from 'lucide-react';
import ThemeToggle from '../components/ThemeToggle';

const features = [
  { icon: Zap, title: 'AI-Powered Generation', desc: 'Our intelligent engine automatically schedules classes, avoiding conflicts and optimizing resource usage.', color: 'var(--brand-primary)', bg: 'rgba(79,142,247,0.1)' },
  { icon: Users, title: 'Organization Sync', desc: 'Manage multiple departments and teams with robust permission boundaries and real-time updates.', color: 'var(--brand-secondary)', bg: 'rgba(155,114,247,0.1)' },
  { icon: Calendar, title: 'Dynamic Timetables', desc: 'Visual weekly grids, recurring patterns, and instant publishing for your entire organization.', color: 'var(--brand-accent)', bg: 'rgba(16,217,160,0.1)' },
  { icon: Shield, title: 'Smart Conflict Detection', desc: 'Real-time conflict checking ensures no double-bookings for resources, rooms, or teachers.', color: 'var(--brand-warning)', bg: 'rgba(247,169,79,0.1)' },
  { icon: BarChart3, title: 'Analytics Dashboard', desc: 'Gain insights into scheduling efficiency, resource utilization, and team productivity.', color: 'var(--brand-primary)', bg: 'rgba(79,142,247,0.1)' },
  { icon: Clock, title: 'Flexible Time Slots', desc: 'Configure custom time slots, break periods, and recurring weekly patterns with ease.', color: 'var(--brand-secondary)', bg: 'rgba(155,114,247,0.1)' },
];

const highlights = ['No scheduling conflicts', 'Automated timetable generation', 'Role-based access control', 'Real-time updates'];

const Home = () => {
  return (
    <div className="page-wrapper" style={{ background: 'var(--bg-primary)' }}>
      {/* Navigation */}
      <nav style={{ padding: '1.25rem 2rem', borderBottom: '1px solid var(--border-color)', position: 'sticky', top: 0, zIndex: 100, backdropFilter: 'blur(16px)', background: 'var(--bg-glass)', transition: 'background 0.35s ease' }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ background: 'var(--brand-gradient)', padding: '0.5rem', borderRadius: 'var(--radius-sm)', boxShadow: '0 4px 12px rgba(79,142,247,0.4)' }}>
              <Calendar color="white" size={20} />
            </div>
            <span style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.02em' }}>Schedulo</span>
          </div>
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
            <ThemeToggle />
            <Link to="/login" className="btn btn-secondary" style={{ padding: '0.6rem 1.25rem', fontSize: '0.875rem' }}>Login</Link>
            <Link to="/signup" className="btn btn-primary" style={{ padding: '0.6rem 1.25rem', fontSize: '0.875rem' }}>Get Started Free</Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section style={{ padding: '6rem 2rem 4rem', textAlign: 'center', position: 'relative', overflow: 'hidden' }}>
        <div style={{ maxWidth: '800px', margin: '0 auto', position: 'relative', zIndex: 1 }}>
          {/* Live badge */}
          <div className="animate-fade-in" style={{ marginBottom: '2rem', display: 'inline-flex', alignItems: 'center', gap: '0.6rem', padding: '0.4rem 1.25rem', background: 'rgba(79,142,247,0.1)', border: '1px solid rgba(79,142,247,0.25)', borderRadius: '2rem', fontSize: '0.82rem', fontWeight: 600, color: 'var(--brand-primary)' }}>
            <span style={{ width:8, height:8, background:'var(--brand-primary)', borderRadius:'50%', boxShadow:'0 0 10px var(--brand-primary)', animation:'pulse 2s infinite', display:'inline-block' }} />
            Schedulo 2.0 — Now Live
          </div>

          <h1 className="animate-slide-up" style={{ fontSize: 'clamp(2.5rem, 6vw, 4.5rem)', fontWeight: 900, marginBottom: '1.5rem', lineHeight: 1.05, letterSpacing: '-0.03em' }}>
            Intelligent scheduling
            <br />
            <span className="text-gradient">built for teams</span>
          </h1>

          <p className="animate-slide-up delay-100" style={{ fontSize: 'clamp(1rem, 2vw, 1.2rem)', color: 'var(--text-secondary)', maxWidth: '560px', margin: '0 auto 2.5rem', lineHeight: 1.7 }}>
            Generate conflict-free timetables in seconds with our AI engine. Designed for schools, colleges, hospitals, and enterprises.
          </p>

          <div className="animate-slide-up delay-200" style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap', marginBottom: '3rem' }}>
            <Link to="/signup" className="btn btn-primary btn-lg" style={{ borderRadius: '3rem', gap: '0.5rem' }}>
              Start Free Today <ArrowRight size={18}/>
            </Link>
            <Link to="/login" className="btn btn-secondary btn-lg" style={{ borderRadius: '3rem', background: 'rgba(255,255,255,0.04)' }}>
              Sign In
            </Link>
          </div>

          {/* Highlights */}
          <div className="animate-fade-in delay-300" style={{ display: 'flex', gap: '1.5rem', justifyContent: 'center', flexWrap: 'wrap' }}>
            {highlights.map(h => (
              <div key={h} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                <CheckCircle size={14} color="var(--brand-accent)" /> {h}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section style={{ padding: '4rem 2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <h2 style={{ fontSize: 'clamp(1.75rem, 3vw, 2.5rem)', marginBottom: '0.75rem' }}>Everything you need to <span className="text-gradient">schedule smarter</span></h2>
          <p style={{ fontSize: '1.05rem', maxWidth: '500px', margin: '0 auto' }}>A complete platform for intelligent resource scheduling and team coordination.</p>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
          {features.map((f, i) => (
            <div key={f.title} className={`glass-card animate-slide-up delay-${(i % 3 + 1) * 100}`}>
              <div style={{ width: '48px', height: '48px', borderRadius: 'var(--radius-md)', background: f.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1.25rem' }}>
                <f.icon size={24} color={f.color} />
              </div>
              <h3 style={{ fontSize: '1.1rem', marginBottom: '0.6rem' }}>{f.title}</h3>
              <p style={{ fontSize: '0.9rem', lineHeight: 1.65, margin: 0 }}>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section style={{ padding: '5rem 2rem', textAlign: 'center' }}>
        <div style={{ maxWidth: '600px', margin: '0 auto', padding: '3rem', background: 'linear-gradient(135deg, rgba(79,142,247,0.12), rgba(155,114,247,0.1))', border: '1px solid rgba(79,142,247,0.2)', borderRadius: 'var(--radius-xl)' }}>
          <Star size={36} color="var(--brand-primary)" style={{ marginBottom: '1rem' }} />
          <h2 style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>Ready to transform scheduling?</h2>
          <p style={{ marginBottom: '2rem' }}>Join organizations worldwide using Schedulo to save hours every week.</p>
          <Link to="/signup" className="btn btn-primary btn-lg" style={{ borderRadius: '3rem' }}>
            Get Started — It's Free <ArrowRight size={18}/>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--border-color)', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
        © 2026 Schedulo · Intelligent Scheduling Platform · Built with ❤️
      </footer>
    </div>
  );
};

export default Home;
