import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, setAuthToken } from '../services/api';
import { LogIn } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await api.login({ email, password });
      if (res.accessToken || res.token) {
        setAuthToken(res.accessToken || res.token);
        window.location.href = '/dashboard';
      } else {
        setError('Login failed: No access token received');
      }
    } catch (err: any) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center w-full" style={{ minHeight: '60vh' }}>
      <div className="glass-panel p-8 max-w-md w-full animate-fade-in">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-2">
            <LogIn size={32} className="text-indigo-400" />
            <h2 className="text-gradient m-0" style={{ fontSize: '2rem', fontWeight: 800 }}>Welcome Back</h2>
          </div>
          <p style={{ color: 'rgba(255,255,255,0.5)' }}>Login with your creator account</p>
        </div>

        {error && (
          <div className="mb-4 p-4" style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', borderRadius: 8, border: '1px solid rgba(239,68,68,0.2)' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="flex flex-col gap-4">
          <div>
            <label className="label">Email Address</label>
            <input 
              type="email" 
              className="input-field" 
              placeholder="you@example.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required 
            />
          </div>
          <div>
            <label className="label">Password</label>
            <input 
              type="password" 
              className="input-field" 
              placeholder="••••••••"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required 
            />
          </div>
          <button type="submit" className="btn btn-primary mt-4" disabled={loading} style={{ padding: '12px' }}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
}
