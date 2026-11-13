import { api } from './client'
const d = (r) => r.data
export const challengesApi = {
  list: () => api.get('/api/challenges').then(d),
  get: (id) => api.get(`/api/challenges/${id}`).then(d),
  submit: ({ id, content }) => api.post(`/api/challenges/${id}/submit`, { content }).then(d),
  mine: () => api.get('/api/challenges/me/submissions').then(d),
}
