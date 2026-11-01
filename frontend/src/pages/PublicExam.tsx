import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../services/api';
import { Clock, Calendar, CheckCircle, AlertCircle } from 'lucide-react';

export default function PublicExam() {
  const { shareCode } = useParams();
  const [exam, setExam] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchExam = async () => {
      try {
        if (shareCode) {
          const data = await api.getPublicExam(shareCode);
          setExam(data);
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load exam. The link might be invalid or expired.');
      } finally {
        setLoading(false);
      }
    };
    fetchExam();
  }, [shareCode]);

  if (loading) return (
    <div className="flex items-center justify-center w-full" style={{ minHeight: '60vh' }}>
      <div className="animate-fade-in" style={{ color: 'rgba(255,255,255,0.6)' }}>Loading examination details...</div>
    </div>
  );

  if (error || !exam) return (
    <div className="flex items-center justify-center w-full" style={{ minHeight: '60vh' }}>
      <div className="glass-panel p-8 max-w-md w-full text-center animate-fade-in">
        <AlertCircle size={48} className="text-red-500 mx-auto mb-4" />
        <h2 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>Unavailable</h2>
        <p style={{ color: 'rgba(255,255,255,0.6)' }}>{error || 'Exam not found'}</p>
      </div>
    </div>
  );

  return (
    <div className="max-w-4xl w-full mx-auto pb-12 pt-8 animate-fade-in">
      <div className="glass-panel p-10 flex flex-col items-center text-center">
        <CheckCircle size={48} className="text-emerald-400 mb-6" />
        <h1 className="text-gradient" style={{ fontSize: '2.5rem', marginBottom: '16px', fontWeight: 800 }}>
          {exam.title}
        </h1>
        
        <p style={{ fontSize: '1.1rem', color: 'rgba(255,255,255,0.8)', maxWidth: '600px', margin: '0 auto 32px' }}>
          {exam.description || 'No description provided for this examination.'}
        </p>

        <div className="flex flex-wrap justify-center gap-6 mb-10">
          <div className="glass-panel px-6 py-4 flex flex-col items-center">
            <Clock size={24} className="text-indigo-400 mb-2" />
            <span style={{ fontSize: '0.9rem', color: 'rgba(255,255,255,0.5)' }}>Duration</span>
            <span style={{ fontSize: '1.2rem', fontWeight: 600 }}>{exam.durationMinutes} min</span>
          </div>
          
          {(exam.startTime || exam.endTime) && (
            <div className="glass-panel px-6 py-4 flex flex-col items-center">
              <Calendar size={24} className="text-purple-400 mb-2" />
              <span style={{ fontSize: '0.9rem', color: 'rgba(255,255,255,0.5)' }}>Available</span>
              <span style={{ fontSize: '1.1rem', fontWeight: 600 }}>
                {exam.startTime ? new Date(exam.startTime).toLocaleDateString() : 'Now'} 
                {exam.endTime && ` - ${new Date(exam.endTime).toLocaleDateString()}`}
              </span>
            </div>
          )}
        </div>

        <div className="w-full max-w-md pt-8" style={{ borderTop: '1px solid rgba(255,255,255,0.1)' }}>
          <button className="btn btn-primary w-full" style={{ padding: '16px', fontSize: '1.1rem' }}>
            Begin Examination
          </button>
          <p style={{ marginTop: '16px', fontSize: '0.85rem', color: 'rgba(255,255,255,0.4)' }}>
            Once you click begin, your timer will start automatically. Good luck!
          </p>
        </div>
      </div>
    </div>
  );
}
