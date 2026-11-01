import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { getAuthToken } from './services/api';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ExamEditor from './pages/ExamEditor';
import PublicExam from './pages/PublicExam';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const token = getAuthToken();
  if (!token) return <Navigate to="/login" />;
  return <>{children}</>;
};

function App() {
  return (
    <BrowserRouter>
      <div className="app-container">
        <header className="app-header">
          <div className="container flex items-center justify-between">
            <h1 className="text-gradient" style={{ margin: 0, fontSize: '1.5rem', fontWeight: 800 }}>
              ExamPortal Creator
            </h1>
            {getAuthToken() && (
              <button 
                className="btn btn-secondary"
                onClick={() => {
                  localStorage.removeItem('token');
                  window.location.href = '/login';
                }}
              >
                Logout
              </button>
            )}
          </div>
        </header>

        <main className="container flex-1 animate-fade-in">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/exam/public/:shareCode" element={<PublicExam />} />
            
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/exam/new" element={
              <ProtectedRoute>
                <ExamEditor />
              </ProtectedRoute>
            } />
            <Route path="/exam/edit/:id" element={
              <ProtectedRoute>
                <ExamEditor />
              </ProtectedRoute>
            } />
            
            <Route path="*" element={<Navigate to="/dashboard" />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
