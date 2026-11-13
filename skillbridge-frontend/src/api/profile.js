import { api } from './client'
const d = (r) => r.data
export const profileApi = {
  mine: () => api.get('/api/users/me/profile').then(d),
  update: (body) => api.put('/api/users/me/profile', body).then(d),
  publicProfile: (userId) => api.get(`/api/users/${userId}/profile`).then(d),
  list: (userId, kind) => api.get(`/api/users/${userId}/${kind}`).then(d), // skills|education|experience|certifications|portfolio
  create: (kind, body) => api.post(`/api/users/me/${kind}`, body).then(d),
  update_item: (kind, { id, ...body }) => api.put(`/api/${kind}/${id}`, body).then(d),
  remove: (kind, id) => api.delete(`/api/${kind}/${id}`),
  attachSkill: (skillId) => api.post(`/api/users/me/skills/${skillId}`).then(d),
  detachSkill: (skillId) => api.delete(`/api/users/me/skills/${skillId}`),
}
