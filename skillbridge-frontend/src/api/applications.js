import { api } from './client'
const d = (r) => r.data
export const applicationsApi = {
  list: (params) => api.get('/api/applications', { params }).then(d),
  forJob: (jobId, params) => api.get(`/api/jobs/${jobId}/applications`, { params, skipForbiddenRedirect: true }).then(d),
  get: (id) => api.get(`/api/applications/${id}`).then(d),
  apply: ({ jobId, ...body }) => api.post(`/api/jobs/${jobId}/applications`, body).then(d),
  setStatus: ({ id, status }) => api.put(`/api/applications/${id}/status`, { status }).then(d),
  withdraw: (id) => api.put(`/api/applications/${id}/withdraw`).then(d),
  remove: (id) => api.delete(`/api/applications/${id}`),
}
