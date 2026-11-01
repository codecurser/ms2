import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { Plus, Trash2, Globe, Clock, Copy, Calendar } from 'lucide-react';

export default function Dashboard() {
  const [exams, setExams] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchExams = async () => {
    try {
      const data = await api.getExams();
      console.log('Fetched exams response:', data);
      const list = data.exams || data.content || (Array.isArray(data) ? data : []);
      setExams(list);
    } catch (err) {
      console.error('Failed to fetch exams:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExams();
  }, []);

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this draft?')) {
      try {
        await api.deleteExam(id);
        fetchExams();
      } catch (err) {
        alert('Cannot delete this exam');
      }
    }
  };

  const handlePublish = async (id: string) => {
    if (confirm('Are you sure you want to publish this exam? It cannot be reverted to draft.')) {
      try {
        await api.publishExam(id);
        fetchExams();
      } catch (err) {
        alert('Cannot publish exam yet.');
      }
    }
  };

  const getStatusBadge = (status: string) => {
    const s = status.toLowerCase();
    return <span className={`badge badge-${s}`}>{status}</span>;
  };

  const copyToClipboard = async (code: string) => {
    await navigator.clipboard.writeText(code);
    alert('Share code copied: ' + code);
  };

  if (loading) return <div className="text-center mt-8">Loading exams...</div>;

  return (
    <div className="w-full">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h2 style={{ fontSize: '2rem', marginBottom: '8px' }}>Your Exams</h2>
          <p style={{ color: 'rgba(255,255,255,0.5)', margin: 0 }}>Manage and publish your examinations</p>
        </div>
        <Link to="/exam/new" className="btn btn-primary">
          <Plus size={18} /> Create New Exam
        </Link>
      </div>

      {exams.length === 0 ? (
        <div className="glass-panel p-8 text-center" style={{ borderStyle: 'dashed' }}>
          <p style={{ color: 'rgba(255,255,255,0.5)' }}>You haven't created any exams yet.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
          {exams.map(exam => (
            <div key={exam.id} className="glass-panel p-6 flex flex-col justify-between animate-fade-in">
              <div>
                <div className="flex justify-between items-start mb-4">
                  <h3 style={{ fontSize: '1.25rem', margin: 0, fontWeight: 600 }}>{exam.title}</h3>
                  {getStatusBadge(exam.status)}
                </div>
                
                <div className="flex items-center gap-2 mb-2" style={{ color: 'rgba(255,255,255,0.6)', fontSize: '0.85rem' }}>
                  <Clock size={14} />
                  <span>{exam.durationMinutes} minutes</span>
                </div>
                
                <div className="flex items-center gap-2 mb-4" style={{ color: 'rgba(255,255,255,0.6)', fontSize: '0.85rem' }}>
                  <Calendar size={14} />
                  <span>Created {new Date(exam.createdAt).toLocaleDateString()}</span>
                </div>
              </div>

              <div className="flex gap-2 mt-4 pt-4" style={{ borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                {exam.status === 'DRAFT' ? (
                  <>
                    <Link to={`/exam/edit/${exam.id}`} className="btn btn-secondary flex-1" style={{ fontSize: '0.8rem' }}>Edit</Link>
                    <button onClick={() => handlePublish(exam.id)} className="btn btn-primary flex-1" style={{ fontSize: '0.8rem' }}><Globe size={14}/> Publish</button>
                    <button onClick={() => handleDelete(exam.id)} className="btn btn-danger" style={{ padding: '10px' }}><Trash2 size={16}/></button>
                  </>
                ) : (
                  <>
                    <Link to={`/exam/edit/${exam.id}`} className="btn btn-secondary flex-1" style={{ fontSize: '0.8rem' }}>View Details</Link>
                    <button onClick={() => copyToClipboard(exam.shareCode || 'Unknown')} className="btn btn-primary flex-1" style={{ fontSize: '0.8rem', background: '#10b981' }}>
                      <Copy size={14}/> Share Code
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
