import React, { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send, Bot, User } from 'lucide-react';

const ChatBot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    { id: 1, text: "Hi! I'm Schedulo AI. Ask me about your timetables, team members, or rooms!", sender: 'bot' }
  ]);
  const [input, setInput] = useState('');
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isOpen]);

  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMsg = input.trim();
    setMessages(prev => [...prev, { id: Date.now(), text: userMsg, sender: 'user' }]);
    setInput('');

    // NLP Engine (Simulated)
    setTimeout(() => {
      let botResponse = "I'm sorry, I didn't quite catch that. Try asking about 'teachers', 'conflict', or 'export'.";
      const q = userMsg.toLowerCase();
      
      if (q.includes('teacher') || q.includes('who')) {
        botResponse = "We have 12 active teachers in the system right now. Your busiest teacher is 'Dr. Smith' with 15 periods this week.";
      } else if (q.includes('room') || q.includes('where')) {
        botResponse = "Room A-101 has the highest utilization at 90%. All science labs are booked for Friday.";
      } else if (q.includes('export') || q.includes('pdf') || q.includes('excel') || q.includes('csv') || q.includes('download')) {
        botResponse = "To export, go to the Timetables page, click on any generated timetable, and you'll see the 'PDF' and 'CSV' buttons at the top!";
      } else if (q.includes('conflict') || q.includes('double book') || q.includes('ai')) {
        botResponse = "Schedulo guarantees zero conflicts! Our AI engine uses a backtracking algorithm to ensure no teacher or room is double-booked anywhere.";
      } else if (q.includes('hello') || q.includes('hi')) {
        botResponse = "Hello! How can I help you schedule today?";
      }

      setMessages(prev => [...prev, { id: Date.now() + 1, text: botResponse, sender: 'bot' }]);
    }, 1000);
  };

  return (
    <>
      <button 
        className="btn-primary no-print"
        onClick={() => setIsOpen(true)}
        style={{
          position: 'fixed', bottom: '2rem', right: '2rem',
          width: '60px', height: '60px', borderRadius: '50%',
          display: isOpen ? 'none' : 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: 'var(--shadow-lg)', zIndex: 9999, border: 'none', cursor: 'pointer'
        }}
      >
        <MessageSquare size={24} />
      </button>

      {isOpen && (
        <div className="glass-panel no-print animate-slide-up" style={{
          position: 'fixed', bottom: '2rem', right: '2rem',
          width: '340px', height: '480px',
          display: 'flex', flexDirection: 'column',
          zIndex: 10000, overflow: 'hidden'
        }}>
          {/* Header */}
          <div style={{ background: 'var(--brand-gradient)', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'white' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
              <Bot size={20} /> Schedulo AI
            </div>
            <button onClick={() => setIsOpen(false)} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer' }}><X size={18} /></button>
          </div>

          {/* Messages */}
          <div style={{ flex: 1, padding: '1rem', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {messages.map(m => (
              <div key={m.id} style={{ display: 'flex', gap: '0.5rem', flexDirection: m.sender === 'user' ? 'row-reverse' : 'row' }}>
                <div style={{ width: '28px', height: '28px', borderRadius: '50%', background: m.sender === 'user' ? 'var(--bg-secondary)' : 'var(--brand-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  {m.sender === 'user' ? <User size={14} /> : <Bot size={14} />}
                </div>
                <div style={{
                  background: m.sender === 'user' ? 'var(--brand-gradient-soft)' : 'var(--bg-secondary)',
                  border: `1px solid ${m.sender === 'user' ? 'rgba(79,70,229,0.3)' : 'var(--border-color)'}`,
                  padding: '0.75rem', borderRadius: '0.5rem', fontSize: '0.85rem',
                  maxWidth: '80%', lineHeight: 1.4
                }}>
                  {m.text}
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <form onSubmit={handleSend} style={{ padding: '0.75rem', borderTop: '1px solid var(--border-color)', display: 'flex', gap: '0.5rem', background: 'var(--bg-primary)' }}>
            <input 
              type="text" className="input-field" 
              placeholder="Ask me anything..." 
              value={input} onChange={e => setInput(e.target.value)}
              style={{ flex: 1, padding: '0.6rem 1rem', borderRadius: '2rem' }}
            />
            <button type="submit" disabled={!input.trim()} className="btn-primary" style={{ width: '40px', height: '40px', borderRadius: '50%', padding: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, border: 'none', cursor: 'pointer' }}>
              <Send size={16} />
            </button>
          </form>
        </div>
      )}
    </>
  );
};

export default ChatBot;
