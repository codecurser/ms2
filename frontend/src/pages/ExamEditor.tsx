import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../services/api';
import { Save, ArrowLeft } from 'lucide-react';

export default function ExamEditor() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = !!id;

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    durationMinutes: 60,
    startTime: '',
    endTime: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isEditing && id) {
      const fetchExam = async () => {
        try {
          const data = await api.getExamById(id);
          setFormData({
            title: data.title || '',
            description: data.description || '',
            durationMinutes: data.durationMinutes || 60,
            startTime: data.startTime ? data.startTime.slice(0, 16) : '',
            endTime: data.endTime ? data.endTime.slice(0, 16) : ''
          });
        } catch (err) {
          setError('Failed to load exam details');
        }
      };
      fetchExam();
    }
  }, [id, isEditing]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    // Format dates for API
    const payload = {
      ...formData,
      startTime: formData.startTime ? new Date(formData.startTime).toISOString() : null,
      endTime: formData.endTime ? new Date(formData.endTime).toISOString() : null,
    };

    try {
      if (isEditing && id) {
        await api.updateExam(id, payload);
      } else {
        await api.createExam(payload);
      }
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to save exam');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl w-full mx-auto pb-12 animate-fade-in">
      <div className="flex items-center gap-4 mb-8">
        <button onClick={() => navigate('/dashboard')} className="btn btn-secondary" style={{ padding: '8px 12px' }}>
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 style={{ fontSize: '2rem', marginBottom: 0 }}>{isEditing ? 'Edit Exam' : 'Create New Exam'}</h2>
          <p style={{ color: 'rgba(255,255,255,0.5)', margin: 0 }}>Define the details and constraints of your examination.</p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4" style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', borderRadius: 8, border: '1px solid rgba(239,68,68,0.2)' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="glass-panel p-8 flex flex-col gap-6">
        <div>
          <label className="label">Exam Title *</label>
          <input 
            type="text" 
            className="input-field" 
            placeholder="e.g. Midterm Computer Science"
            value={formData.title}
            onChange={e => setFormData({...formData, title: e.target.value})}
            required
            maxLength={200}
          />
        </div>

        <div>
          <label className="label">Description</label>
          <textarea 
            className="input-field" 
            placeholder="Provide instructions or a brief summary..."
            value={formData.description}
            onChange={e => setFormData({...formData, description: e.target.value})}
            rows={4}
            style={{ resize: 'vertical' }}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
          <div>
            <label className="label">Duration (Minutes) *</label>
            <input 
              type="number" 
              className="input-field" 
              value={formData.durationMinutes}
              onChange={e => setFormData({...formData, durationMinutes: parseInt(e.target.value) || 0})}
              required
              min={1}
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
             {/* Spacing for alignment if needed */}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
          <div>
            <label className="label">Start Time (Optional)</label>
            <input 
              type="datetime-local" 
              className="input-field" 
              value={formData.startTime}
              onChange={e => setFormData({...formData, startTime: e.target.value})}
            />
          </div>
          <div>
            <label className="label">End Time (Optional)</label>
            <input 
              type="datetime-local" 
              className="input-field" 
              value={formData.endTime}
              onChange={e => setFormData({...formData, endTime: e.target.value})}
            />
          </div>
        </div>

        <div className="flex justify-end mt-4 pt-6" style={{ borderTop: '1px solid rgba(255,255,255,0.1)' }}>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            <Save size={18} /> {loading ? 'Saving...' : 'Save Exam Draft'}
          </button>
        </div>
      </form>
    </div>
  );
}
