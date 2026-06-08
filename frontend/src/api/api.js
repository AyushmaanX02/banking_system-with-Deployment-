import axios from 'axios'

const BASE = '/api'

// ── Axios instance ────────────────────────────────────────────────────
const http = axios.create({ baseURL: BASE })

// Attach JWT to every request
http.interceptors.request.use(config => {
  const token = localStorage.getItem('nexbank_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Global 401 handler → redirect to login
http.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('nexbank_token')
      window.location.href = '/react/'
    }
    return Promise.reject(err)
  }
)

// ── Auth ─────────────────────────────────────────────────────────────
export const authApi = {
  login:    data => http.post('/auth/login', data).then(r => r.data),
  register: data => http.post('/auth/register', data).then(r => r.data),
}

// ── Accounts ─────────────────────────────────────────────────────────
export const accountApi = {
  getAll:    ()           => http.get('/accounts').then(r => r.data),
  getOne:    (num)        => http.get(`/accounts/${num}`).then(r => r.data),
  create:    (data)       => http.post('/accounts', data).then(r => r.data),
}

// ── Transactions ──────────────────────────────────────────────────────
export const transactionApi = {
  deposit:  data        => http.post('/transactions/deposit', data).then(r => r.data),
  withdraw: data        => http.post('/transactions/withdraw', data).then(r => r.data),
  transfer: data        => http.post('/transactions/transfer', data).then(r => r.data),
  history:  (accNum)   => http.get(`/transactions/history/${accNum}`).then(r => r.data),
}

// ── Beneficiaries ─────────────────────────────────────────────────────
export const beneficiaryApi = {
  getAll: ()       => http.get('/beneficiaries').then(r => r.data),
  add:    data     => http.post('/beneficiaries', data).then(r => r.data),
  delete: id       => http.delete(`/beneficiaries/${id}`).then(r => r.data),
}

// ── Analytics ────────────────────────────────────────────────────────
export const analyticsApi = {
  summary: () => http.get('/analytics/summary').then(r => r.data),
}

// ── Admin ─────────────────────────────────────────────────────────────
export const adminApi = {
  dashboard:    ()      => http.get('/admin/dashboard').then(r => r.data),
  users:        ()      => http.get('/admin/users').then(r => r.data),
  transactions: ()      => http.get('/admin/transactions').then(r => r.data),
  auditLogs:    ()      => http.get('/admin/audit-logs').then(r => r.data),
  freeze:       (num)   => http.put(`/admin/accounts/${num}/freeze`).then(r => r.data),
  unfreeze:     (num)   => http.put(`/admin/accounts/${num}/unfreeze`).then(r => r.data),
}

// ── Customer ──────────────────────────────────────────────────────────
export const customerApi = {
  profile:     ()      => http.get('/customers/profile').then(r => r.data),
  updatePhone: (phone) => http.put(`/customers/profile/phone?phone=${phone}`).then(r => r.data),
}
