import { api } from './client'
const clean = (p = {}) => Object.fromEntries(Object.entries(p).filter(([, v]) => v !== '' && v != null))
const d = (r) => r.data
export const jobsApi = {
  list: (params) => api.get('/api/jobs', { params: clean(params) }).then(d),
  get: (id) => api.get(`/api/jobs/${id}`).then(d),
  skills: (category) => api.get('/api/skills', { params: clean({ category }) }).then(d),
  create: (body) => api.post('/api/jobs', body).then(d),
  update: ({ id, ...body }) => api.put(`/api/jobs/${id}`, body).then(d),
  setStatus: ({ id, status }) => api.put(`/api/jobs/${id}/status`, { status }).then(d),
  remove: (id) => api.delete(`/api/jobs/${id}`),
}
