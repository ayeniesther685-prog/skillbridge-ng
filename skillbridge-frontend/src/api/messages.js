import { api } from './client'
const d = (r) => r.data
export const messagesApi = {
  conversations: (params) => api.get('/api/conversations', { params }).then(d),
  start: (otherUserId) => api.post('/api/conversations', { otherUserId }).then(d),
  get: (id) => api.get(`/api/conversations/${id}`).then(d),
  messages: (id, params) => api.get(`/api/conversations/${id}/messages`, { params }).then(d),
  send: ({ id, content }) => api.post(`/api/conversations/${id}/messages`, { content }).then(d),
  markRead: (id) => api.put(`/api/messages/${id}/read`),
}
