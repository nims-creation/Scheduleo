import React, { useState, useEffect } from 'react';
import { CreditCard, CheckCircle2, AlertCircle } from 'lucide-react';
import api from '../../services/api';

const Billing = () => {
  const [plans, setPlans] = useState([]);
  const [currentSubscription, setCurrentSubscription] = useState(null);
  const [billingCycle, setBillingCycle] = useState('MONTHLY');
  const [loading, setLoading] = useState(true);

  // Fallback plans
  const defaultPlans = [
    { code: 'FREE', name: 'Free', description: 'For small teams getting started', monthlyPrice: 0, quarterlyPrice: 0, yearlyPrice: 0, maxUsers: 10, maxSchedulesPerDay: 50, planType: 'FREE' },
    { code: 'PRO', name: 'Professional', description: 'For growing organizations needing full power', monthlyPrice: 50, quarterlyPrice: 180, yearlyPrice: 500, maxUsers: 50, maxSchedulesPerDay: 500, planType: 'PROFESSIONAL', isPopular: true },
    { code: 'ENTERPRISE', name: 'Enterprise', description: 'Unlimited everything for large-scale operations', monthlyPrice: 150, quarterlyPrice: 500, yearlyPrice: 1500, maxUsers: 9999, maxSchedulesPerDay: 9999, planType: 'ENTERPRISE' }
  ];

  const fetchBillingData = async () => {
    try {
      setLoading(true);
      const [plansRes, subRes] = await Promise.all([
        api.get('/api/v1/subscriptions/plans').catch(() => ({ data: { data: [] } })),
        api.get('/api/v1/subscriptions/current').catch(() => ({ data: { data: null } }))
      ]);
      
      const latestPlans = (plansRes.data?.data && plansRes.data.data.length > 0) 
        ? plansRes.data.data.map(p => {
            if (p.code === 'PRO' || p.planType === 'PROFESSIONAL') {
              return { ...p, monthlyPrice: 50, quarterlyPrice: 180, yearlyPrice: 500 };
            }
            if (p.code === 'ENTERPRISE' || p.planType === 'ENTERPRISE') {
              return { ...p, monthlyPrice: 150, quarterlyPrice: 500, yearlyPrice: 1500 };
            }
            return { ...p, quarterlyPrice: p.quarterlyPrice || 0 };
          }) 
        : defaultPlans;
      setPlans(latestPlans);
      setCurrentSubscription(subRes.data?.data);
    } catch (err) {
      console.error('Failed to load billing data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBillingData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleUpgrade = async (planCode) => {
    try {
      if(window.confirm(`Are you sure you want to upgrade to ${planCode} on a ${billingCycle} cycle?`)){
          await api.post('/api/v1/subscriptions/upgrade', {
            planCode,
            billingCycle
          });
          alert('Subscription upgraded successfully!');
          fetchBillingData();
      }
    } catch (err) {
      alert('Error upgrading subscription. Please try again.');
    }
  };

  if (loading) {
    return <div style={{ padding: '4rem', textAlign: 'center' }}>Loading billing data...</div>;
  }

  const currentPlanCode = currentSubscription?.plan?.code || 'FREE';

  return (
    <div className="page-wrapper" style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.5rem 0', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <CreditCard size={28} color="var(--brand-primary)" />
            Billing & Subscriptions
          </h1>
          <p style={{ color: 'var(--text-muted)', margin: 0 }}>Manage your organization's plan and payment methods.</p>
        </div>
      </div>

      <div style={{ background: 'var(--bg-secondary)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', padding: '2rem', marginBottom: '2rem' }}>
        <h3 style={{ margin: '0 0 1rem', fontSize: '1.25rem' }}>Current Plan Context</h3>
        {currentSubscription ? (
          <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
            <div style={{ flex: 1, minWidth: '200px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Active Plan</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--brand-primary)' }}>{currentSubscription.plan.name}</div>
              <div style={{ marginTop: '0.5rem', fontSize: '0.875rem' }}>
                <span style={{ 
                  background: currentSubscription.status === 'ACTIVE' ? 'rgba(16, 217, 160, 0.1)' : 'rgba(239, 68, 68, 0.1)', 
                  color: currentSubscription.status === 'ACTIVE' ? 'var(--brand-success)' : 'var(--brand-danger)', 
                  padding: '0.25rem 0.5rem', borderRadius: '4px', fontWeight: 600
                }}>
                  {currentSubscription.status}
                </span>
              </div>
            </div>
            
            <div style={{ flex: 1, minWidth: '200px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Current Cycle</div>
              <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{currentSubscription.billingCycle}</div>
              <div style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
                Renews {new Date(currentSubscription.currentPeriodEnd).toLocaleDateString()}
              </div>
            </div>

            <div style={{ flex: 1, minWidth: '200px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Amount</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 700 }}>₹{currentSubscription.amount || 0}</div>
            </div>
          </div>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--brand-warning)', background: 'rgba(245, 158, 11, 0.1)', padding: '1rem', borderRadius: '8px' }}>
            <AlertCircle size={18} />
            You are currently on the legacy Free Plan. Upgrade below.
          </div>
        )}
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '2rem' }}>
        <div style={{ background: 'var(--bg-secondary)', padding: '0.25rem', borderRadius: '2rem', display: 'flex', border: '1px solid var(--border-color)' }}>
          <button 
            onClick={() => setBillingCycle('MONTHLY')}
            style={{ padding: '0.5rem 1.5rem', borderRadius: '2rem', border: 'none', background: billingCycle === 'MONTHLY' ? 'var(--brand-primary)' : 'transparent', color: billingCycle === 'MONTHLY' ? 'white' : 'var(--text-primary)', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}
          >
            Monthly
          </button>
          <button 
            onClick={() => setBillingCycle('QUARTERLY')}
            style={{ padding: '0.5rem 1.5rem', borderRadius: '2rem', border: 'none', background: billingCycle === 'QUARTERLY' ? 'var(--brand-primary)' : 'transparent', color: billingCycle === 'QUARTERLY' ? 'white' : 'var(--text-primary)', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}
          >
            Quarterly
          </button>
          <button 
            onClick={() => setBillingCycle('YEARLY')}
            style={{ padding: '0.5rem 1.5rem', borderRadius: '2rem', border: 'none', background: billingCycle === 'YEARLY' ? 'var(--brand-primary)' : 'transparent', color: billingCycle === 'YEARLY' ? 'white' : 'var(--text-primary)', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}
          >
            Yearly <span style={{ fontSize: '0.7rem', background: 'rgba(255,255,255,0.2)', padding: '2px 6px', borderRadius: '10px', marginLeft: '4px' }}>Save!</span>
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
        {plans.map((plan) => (
          <div key={plan.code} style={{ 
            background: 'var(--bg-secondary)', 
            borderRadius: 'var(--radius-lg)', 
            border: `2px solid ${plan.isPopular ? 'var(--brand-primary)' : 'var(--border-color)'}`,
            padding: '2rem',
            position: 'relative',
            display: 'flex', flexDirection: 'column'
          }}>
            {plan.isPopular && (
              <div style={{ position: 'absolute', top: '-12px', left: '50%', transform: 'translateX(-50%)', background: 'var(--brand-primary)', color: 'white', padding: '0.25rem 1rem', borderRadius: '2rem', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Most Popular
              </div>
            )}
            
            <h3 style={{ margin: '0 0 0.5rem', fontSize: '1.5rem' }}>{plan.name}</h3>
            <p style={{ color: 'var(--text-muted)', margin: '0 0 1.5rem', minHeight: '40px' }}>{plan.description}</p>
            
            <div style={{ marginBottom: '2rem', display: 'flex', alignItems: 'baseline', gap: '0.25rem' }}>
              <span style={{ fontSize: '2.5rem', fontWeight: 800 }}>
                ₹{billingCycle === 'YEARLY' ? plan.yearlyPrice : billingCycle === 'QUARTERLY' ? plan.quarterlyPrice : plan.monthlyPrice}
              </span>
              <span style={{ color: 'var(--text-muted)' }}>
                /{billingCycle === 'MONTHLY' ? 'mo' : billingCycle === 'QUARTERLY' ? 'quarter' : 'yr'}
              </span>
            </div>
            
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <CheckCircle2 size={18} color="var(--brand-success)" />
                <span>Up to {plan.maxUsers} Users</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <CheckCircle2 size={18} color="var(--brand-success)" />
                <span>{plan.maxSchedulesPerDay} AI Generations/day</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <CheckCircle2 size={18} color="var(--brand-success)" />
                <span>Advanced Timetable Rules</span>
              </div>
            </div>

            <button 
              disabled={currentPlanCode === plan.code && currentSubscription?.billingCycle === billingCycle}
              onClick={() => handleUpgrade(plan.code)}
              className={`btn ${plan.isPopular ? 'btn-primary' : 'btn-outline'}`}
              style={{ width: '100%', padding: '0.75rem', fontSize: '1rem' }}
            >
              {currentPlanCode === plan.code && currentSubscription?.billingCycle === billingCycle 
                  ? 'Current Plan' : 'Select Plan'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Billing;
