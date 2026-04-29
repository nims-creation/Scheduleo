import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import './Home.css';
import {
  Calendar, Clock, Users, ArrowRight, Zap, Shield,
  BarChart3, CheckCircle, Star, ChevronDown, Sparkles,
  Building2, GraduationCap, Hospital, Globe
} from 'lucide-react';
import ThemeToggle from '../components/ThemeToggle';

/* ─── Data ────────────────────────────────────────── */
const features = [
  {
    icon: Zap,
    title: 'AI-Powered Generation',
    desc: 'Our intelligent engine automatically schedules classes, avoiding conflicts and optimizing resource usage in seconds.',
    color: '#06b6d4',
    glow: 'rgba(6,182,212,0.35)',
    bg: 'rgba(6,182,212,0.08)',
  },
  {
    icon: Users,
    title: 'Organization Sync',
    desc: 'Manage multiple departments and teams with robust role-based permissions and real-time live updates.',
    color: '#4F46E5',
    glow: 'rgba(79,70,229,0.35)',
    bg: 'rgba(79,70,229,0.08)',
  },
  {
    icon: Calendar,
    title: 'Dynamic Timetables',
    desc: 'Visual weekly grids, recurring patterns, and one-click publishing for your entire organization.',
    color: '#10d9a0',
    glow: 'rgba(16,217,160,0.35)',
    bg: 'rgba(16,217,160,0.08)',
  },
  {
    icon: Shield,
    title: 'Smart Conflict Detection',
    desc: 'Real-time conflict checking ensures no double-bookings for resources, rooms, or teachers—ever.',
    color: '#f59e0b',
    glow: 'rgba(245,158,11,0.35)',
    bg: 'rgba(245,158,11,0.08)',
  },
  {
    icon: BarChart3,
    title: 'Analytics Dashboard',
    desc: 'Gain insights into scheduling efficiency, resource utilization, and team productivity at a glance.',
    color: '#06b6d4',
    glow: 'rgba(6,182,212,0.35)',
    bg: 'rgba(6,182,212,0.08)',
  },
  {
    icon: Clock,
    title: 'Flexible Time Slots',
    desc: 'Configure custom time slots, break periods, and recurring weekly patterns with total flexibility.',
    color: '#4F46E5',
    glow: 'rgba(79,70,229,0.35)',
    bg: 'rgba(79,70,229,0.08)',
  },
];

const stats = [
  { value: '10K+', label: 'Timetables Generated' },
  { value: '99.9%', label: 'Conflict-Free Rate' },
  { value: '3s', label: 'Avg Generation Time' },
  { value: '500+', label: 'Organizations' },
];

const testimonials = [
  {
    name: 'Dr. Priya Sharma',
    role: 'Academic Director, DU College',
    avatar: 'PS',
    text: 'Schedulo cut our timetable planning from 3 days to under 10 minutes. Absolutely game-changing for our faculty.',
    rating: 5,
  },
  {
    name: 'Rahul Mehta',
    role: 'Operations Head, TechCorp',
    avatar: 'RM',
    text: 'The conflict detection alone saved us countless headaches. Our team scheduling has never been smoother.',
    rating: 5,
  },
  {
    name: 'Sarah Chen',
    role: 'Hospital Administrator',
    avatar: 'SC',
    text: 'Managing shift schedules for 200+ staff was a nightmare before Schedulo. Now it takes minutes.',
    rating: 5,
  },
];

const industries = [
  { icon: GraduationCap, label: 'Education' },
  { icon: Building2, label: 'Enterprise' },
  { icon: Hospital, label: 'Healthcare' },
  { icon: Globe, label: 'Remote Teams' },
];

const highlights = [
  'No scheduling conflicts',
  'Automated timetable generation',
  'Role-based access control',
  'Real-time updates',
];

/* ─── 3D Tilt Card Hook ───────────────────────────── */
function useTilt(ref) {
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const onMove = (e) => {
      const rect = el.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      const cx = rect.width / 2;
      const cy = rect.height / 2;
      const rotX = ((y - cy) / cy) * -8;
      const rotY = ((x - cx) / cx) * 8;
      el.style.transform = `perspective(800px) rotateX(${rotX}deg) rotateY(${rotY}deg) translateZ(10px)`;
    };
    const onLeave = () => {
      el.style.transform = 'perspective(800px) rotateX(0deg) rotateY(0deg) translateZ(0px)';
    };
    el.addEventListener('mousemove', onMove);
    el.addEventListener('mouseleave', onLeave);
    return () => {
      el.removeEventListener('mousemove', onMove);
      el.removeEventListener('mouseleave', onLeave);
    };
  }, [ref]);
}

/* ─── Intersection Observer for reveal ───────────── */
function useReveal() {
  useEffect(() => {
    const els = document.querySelectorAll('.reveal');
    const obs = new IntersectionObserver(
      (entries) => entries.forEach((e) => { if (e.isIntersecting) { e.target.classList.add('revealed'); obs.unobserve(e.target); } }),
      { threshold: 0.12 }
    );
    els.forEach((el) => obs.observe(el));
    return () => obs.disconnect();
  }, []);
}

/* ─── Feature Card ────────────────────────────────── */
function FeatureCard({ feature, delay }) {
  const ref = useRef(null);
  useTilt(ref);
  return (
    <div
      ref={ref}
      className="reveal feature-card"
      style={{ animationDelay: `${delay}ms`, transitionDelay: `${delay}ms` }}
    >
      <div className="feature-icon-wrap" style={{ background: feature.bg, boxShadow: `0 0 20px ${feature.glow}` }}>
        <feature.icon size={24} color={feature.color} />
      </div>
      <div className="feature-card-glow" style={{ background: `radial-gradient(circle at 50% 0%, ${feature.glow} 0%, transparent 70%)` }} />
      <h3 className="feature-title">{feature.title}</h3>
      <p className="feature-desc">{feature.desc}</p>
      <div className="feature-arrow" style={{ color: feature.color }}>
        Learn more <ArrowRight size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />
      </div>
    </div>
  );
}

/* ─── Stat Counter ────────────────────────────────── */
function StatCard({ stat, delay }) {
  return (
    <div className="stat-item reveal" style={{ transitionDelay: `${delay}ms` }}>
      <div className="stat-num">{stat.value}</div>
      <div className="stat-lbl">{stat.label}</div>
    </div>
  );
}

/* ─── Testimonial Card ────────────────────────────── */
function TestimonialCard({ t, delay }) {
  const ref = useRef(null);
  useTilt(ref);
  return (
    <div ref={ref} className="testimonial-card reveal" style={{ transitionDelay: `${delay}ms` }}>
      <div className="testimonial-stars">
        {Array.from({ length: t.rating }).map((_, i) => (
          <Star key={i} size={14} fill="#f59e0b" color="#f59e0b" />
        ))}
      </div>
      <p className="testimonial-text">"{t.text}"</p>
      <div className="testimonial-author">
        <div className="testimonial-avatar">{t.avatar}</div>
        <div>
          <div className="testimonial-name">{t.name}</div>
          <div className="testimonial-role">{t.role}</div>
        </div>
      </div>
    </div>
  );
}

/* ─── Main Page ───────────────────────────────────── */
const Home = () => {
  useReveal();
  const heroRef = useRef(null);
  const [scrollY, setScrollY] = useState(0);

  /* Parallax scroll */
  useEffect(() => {
    const onScroll = () => setScrollY(window.scrollY);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <div className="home-wrapper">
      {/* ── Ambient blobs ── */}
      <div className="blob blob-1" style={{ transform: `translateY(${scrollY * 0.15}px)` }} />
      <div className="blob blob-2" style={{ transform: `translateY(${-scrollY * 0.1}px)` }} />
      <div className="blob blob-3" style={{ transform: `translateY(${scrollY * 0.08}px)` }} />

      {/* ── 3D Floating Orbs ── */}
      <div className="orb orb-a" />
      <div className="orb orb-b" />
      <div className="orb orb-c" />

      {/* ── Particle Grid ── */}
      <div className="particle-grid" aria-hidden="true">
        {Array.from({ length: 20 }).map((_, i) => (
          <div key={i} className="particle" style={{ '--i': i }} />
        ))}
      </div>

      {/* ════════ NAV ════════ */}
      <nav className="home-nav">
        <div className="nav-inner">
          <div className="nav-logo">
            <div className="logo-icon">
              <Calendar color="white" size={20} />
            </div>
            <span className="logo-text">Schedulo</span>
          </div>
          <div className="nav-links">
            <a href="#features" className="nav-link">Features</a>
            <a href="#stats" className="nav-link">Why Us</a>
            <a href="#testimonials" className="nav-link">Reviews</a>
          </div>
          <div className="nav-actions">
            <ThemeToggle />
            <Link to="/login" className="btn btn-secondary" style={{ padding: '0.55rem 1.1rem', fontSize: '0.875rem' }}>Login</Link>
            <Link to="/signup" className="btn btn-primary nav-cta" style={{ padding: '0.55rem 1.25rem', fontSize: '0.875rem' }}>
              Get Started Free <ArrowRight size={15} />
            </Link>
          </div>
        </div>
      </nav>

      {/* ════════ HERO ════════ */}
      <section className="hero-section" ref={heroRef}>
        <div className="hero-inner">
          {/* Live badge */}
          <div className="hero-badge animate-fade-in">
            <Sparkles size={13} />
            <span className="badge-pulse" />
            Schedulo 2.0 — AI-Powered Scheduling is Live
          </div>

          {/* Heading */}
          <h1 className="hero-heading animate-slide-up">
            Intelligent scheduling
            <br />
            <span className="text-gradient-animated">built for teams</span>
          </h1>

          <p className="hero-sub animate-slide-up delay-100">
            Generate conflict-free timetables in seconds. Designed for schools, colleges,
            hospitals, and enterprises that refuse to waste time.
          </p>

          {/* CTAs */}
          <div className="hero-ctas animate-slide-up delay-200">
            <Link to="/signup" className="btn-hero-primary">
              Start Free Today <ArrowRight size={18} />
            </Link>
            <Link to="/login" className="btn-hero-secondary">
              Sign In
            </Link>
          </div>

          {/* Highlights */}
          <div className="hero-highlights animate-fade-in delay-300">
            {highlights.map((h) => (
              <span key={h} className="highlight-item">
                <CheckCircle size={14} color="var(--brand-accent)" /> {h}
              </span>
            ))}
          </div>

          {/* 3D Dashboard Preview */}
          <div className="dashboard-preview animate-slide-up delay-200" style={{ transform: `translateY(${scrollY * 0.05}px)` }}>
            <div className="dashboard-chrome">
              <div className="chrome-dots">
                <span className="dot dot-red" />
                <span className="dot dot-yellow" />
                <span className="dot dot-green" />
              </div>
              <span className="chrome-title">schedulo.app/dashboard</span>
            </div>
            <div className="dashboard-body">
              {/* Fake timetable grid */}
              <div className="fake-sidebar">
                {['Dashboard', 'Timetable', 'Teachers', 'Rooms', 'Subjects'].map((item, i) => (
                  <div key={item} className={`fake-nav-item ${i === 1 ? 'active' : ''}`}>{item}</div>
                ))}
              </div>
              <div className="fake-main">
                <div className="fake-header">
                  <div className="fake-title" />
                  <div className="fake-chips">
                    <div className="fake-chip chip-blue" />
                    <div className="fake-chip chip-green" />
                  </div>
                </div>
                <div className="fake-grid">
                  {['Mon', 'Tue', 'Wed', 'Thu', 'Fri'].map((d) => (
                    <div key={d} className="fake-col">
                      <div className="fake-day">{d}</div>
                      {Array.from({ length: 5 }).map((_, j) => (
                        <div key={j} className={`fake-slot ${Math.random() > 0.45 ? 'filled' : ''}`} style={{ '--slot-i': j }} />
                      ))}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Scroll cue */}
          <div className="scroll-cue">
            <ChevronDown size={20} />
          </div>
        </div>
      </section>

      {/* ════════ TRUSTED BY ════════ */}
      <section className="industries-section reveal" style={{ transitionDelay: '0ms' }}>
        <p className="industries-label">Trusted across industries</p>
        <div className="industries-row">
          {industries.map((industry) => (
            <div key={industry.label} className="industry-pill">
              <industry.icon size={17} /> {industry.label}
            </div>
          ))}
        </div>
      </section>

      {/* ════════ STATS ════════ */}
      <section id="stats" className="stats-section">
        <div className="section-inner">
          <div className="stats-grid">
            {stats.map((s, i) => <StatCard key={s.label} stat={s} delay={i * 80} />)}
          </div>
        </div>
      </section>

      {/* ════════ FEATURES ════════ */}
      <section id="features" className="features-section">
        <div className="section-inner">
          <div className="section-header reveal">
            <div className="section-label">Features</div>
            <h2 className="section-heading">
              Everything you need to <span className="text-gradient">schedule smarter</span>
            </h2>
            <p className="section-sub">
              A complete platform for intelligent resource scheduling and team coordination.
            </p>
          </div>
          <div className="features-grid">
            {features.map((f, i) => <FeatureCard key={f.title} feature={f} delay={i * 80} />)}
          </div>
        </div>
      </section>

      {/* ════════ TESTIMONIALS ════════ */}
      <section id="testimonials" className="testimonials-section">
        <div className="section-inner">
          <div className="section-header reveal">
            <div className="section-label">Reviews</div>
            <h2 className="section-heading">
              Loved by teams <span className="text-gradient">worldwide</span>
            </h2>
          </div>
          <div className="testimonials-grid">
            {testimonials.map((t, i) => <TestimonialCard key={t.name} t={t} delay={i * 100} />)}
          </div>
        </div>
      </section>

      {/* ════════ CTA ════════ */}
      <section className="cta-section">
        <div className="cta-box reveal">
          <div className="cta-glow-top" />
          <div className="cta-glow-bottom" />
          <div className="cta-icon-ring">
            <Sparkles size={28} color="#06b6d4" />
          </div>
          <h2 className="cta-heading">Ready to transform scheduling?</h2>
          <p className="cta-sub">
            Join hundreds of organizations saving hours every week with Schedulo.
          </p>
          <div className="cta-actions">
            <Link to="/signup" className="btn-hero-primary">
              Get Started — It's Free <ArrowRight size={18} />
            </Link>
            <Link to="/login" className="btn-hero-secondary">
              Sign In Instead
            </Link>
          </div>
          <div className="cta-reassurance">
            <CheckCircle size={13} color="var(--brand-accent)" /> No credit card required
            <CheckCircle size={13} color="var(--brand-accent)" style={{ marginLeft: '1.5rem' }} /> Free forever plan
            <CheckCircle size={13} color="var(--brand-accent)" style={{ marginLeft: '1.5rem' }} /> Setup in 2 minutes
          </div>
        </div>
      </section>

      {/* ════════ FOOTER ════════ */}
      <footer className="home-footer">
        <div className="footer-logo">
          <div className="logo-icon logo-icon-sm">
            <Calendar color="white" size={15} />
          </div>
          <span className="footer-brand">Schedulo</span>
        </div>
        <p className="footer-copy">© 2026 Schedulo · Intelligent Scheduling Platform · Built with ❤️</p>
      </footer>
    </div>
  );
};

export default Home;
