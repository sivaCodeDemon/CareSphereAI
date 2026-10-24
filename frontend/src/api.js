const TOKEN_KEY = 'caresphere.token'
const ROLE_KEY = 'caresphere.role'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function getRole() {
  return localStorage.getItem(ROLE_KEY)
}

export function setAuth(token, role) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(ROLE_KEY, role)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
}

async function request(path, options = {}) {
  const token = getToken()
  const isForm = options.body instanceof FormData
  const headers = { ...(options.headers || {}) }
  if (!isForm) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const res = await fetch(path, { ...options, headers })
  if (res.status === 401) {
    clearAuth()
    window.dispatchEvent(new Event('unauthorized'))
    throw new Error('Authentication required')
  }
  if (!res.ok) {
    let msg = res.statusText
    try {
      const body = await res.json()
      msg = body.message || msg
    } catch {
      /* ignore */
    }
    throw new Error(msg)
  }
  if (res.status === 204) {
    return null
  }
  return res.json()
}

export const api = {
  login: (email, password) =>
    request('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  register: (email, password, fullName) =>
    request('/api/auth/register', { method: 'POST', body: JSON.stringify({ email, password, fullName }) }),
  listPatients: (search, page = 0, size = 10) =>
    request(`/api/patients?page=${page}&size=${size}${search ? `&search=${encodeURIComponent(search)}` : ''}`),
  getPatient: (id) => request(`/api/patients/${id}`),
  createPatient: (data) => request('/api/patients', { method: 'POST', body: JSON.stringify(data) }),
  listRecords: (patientId, page = 0, size = 10) =>
    request(`/api/patients/${patientId}/records?page=${page}&size=${size}`),
  addRecord: (patientId, data) =>
    request(`/api/patients/${patientId}/records`, { method: 'POST', body: JSON.stringify(data) }),
  listDocuments: (patientId, page = 0, size = 10) =>
    request(`/api/patients/${patientId}/documents?page=${page}&size=${size}`),
  uploadDocument: (patientId, file) => {
    const form = new FormData()
    form.append('file', file)
    return request(`/api/patients/${patientId}/documents`, { method: 'POST', body: form })
  },
  indexDocument: (patientId, documentId) =>
    request(`/api/patients/${patientId}/documents/${documentId}/index`, { method: 'POST' }),
  ask: (question, patientId) =>
    request('/api/ai/answer', { method: 'POST', body: JSON.stringify({ question, patientId: patientId || null }) }),
  listAudit: (page = 0, size = 25) => request(`/api/admin/audit?page=${page}&size=${size}`)
}
