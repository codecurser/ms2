const EXAM_API_BASE = import.meta.env.VITE_EXAM_API_BASE || 'https://ms2-w51n.onrender.com/api/v1';
const AUTH_API_BASE = import.meta.env.VITE_AUTH_API_BASE || 'https://microservice-based-examination-1.onrender.com/api/v1/auth';

export const getAuthToken = () => localStorage.getItem('token');
export const setAuthToken = (token: string) => localStorage.setItem('token', token);
export const removeAuthToken = () => localStorage.removeItem('token');

const handleResponse = async (res: Response) => {
  if (!res.ok) {
    if (res.status === 401) {
      console.warn('Unauthorized (401), session expired');
      removeAuthToken();
      window.location.href = '/login';
    }
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || (res.status === 403 ? 'Access forbidden (403)' : 'API request failed'));
  }
  return res.json();
};

export const api = {
  // Auth
  login: async (credentials: any) => {
    const res = await fetch(`${AUTH_API_BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    });
    return handleResponse(res);
  },

  // Exams
  getExams: async (page = 0, size = 20) => {
    const res = await fetch(`${EXAM_API_BASE}/exams?page=${page}&size=${size}`, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(res);
  },
  
  getExamById: async (id: string) => {
    const res = await fetch(`${EXAM_API_BASE}/exams/${id}`, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(res);
  },

  createExam: async (data: any) => {
    const res = await fetch(`${EXAM_API_BASE}/exams`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },

  updateExam: async (id: string, data: any) => {
    const res = await fetch(`${EXAM_API_BASE}/exams/${id}`, {
      method: 'PUT',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },

  deleteExam: async (id: string) => {
    const res = await fetch(`${EXAM_API_BASE}/exams/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });
    if (!res.ok) throw new Error('Failed to delete');
    return true; // DELETE often returns 204 No Content
  },

  publishExam: async (id: string) => {
    const res = await fetch(`${EXAM_API_BASE}/exams/${id}/publish`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(res);
  },

  getShareLink: async (id: string) => {
    const res = await fetch(`${EXAM_API_BASE}/exams/${id}/share-link`, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(res);
  },

  // Public Exams (for students)
  getPublicExam: async (shareCode: string) => {
    const res = await fetch(`${EXAM_API_BASE}/public/exams/${shareCode}`);
    return handleResponse(res);
  }
};
