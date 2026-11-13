import { api } from './client'
const d = (r) => r.data
export const contractsApi = {
  list: (params) => api.get('/api/contracts', { params }).then(d),
  get: (id) => api.get(`/api/contracts/${id}`).then(d),
  create: (body) => api.post('/api/contracts', body).then(d),
  setStatus: ({ id, status }) => api.put(`/api/contracts/${id}/status`, { status }).then(d),
  milestones: (id) => api.get(`/api/contracts/${id}/milestones`).then(d),
  addMilestone: ({ contractId, ...body }) => api.post(`/api/contracts/${contractId}/milestones`, body).then(d),
  updateMilestone: ({ id, ...body }) => api.put(`/api/milestones/${id}`, body).then(d),
  setMilestoneStatus: ({ id, status }) => api.put(`/api/milestones/${id}/status`, { status }).then(d),
  removeMilestone: (id) => api.delete(`/api/milestones/${id}`),
}
// kept for existing imports
export { applicationsApi } from './applications'
