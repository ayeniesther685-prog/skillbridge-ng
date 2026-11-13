import { api } from './client'
export const notificationsApi = {
  unreadCount: () => api.get('/api/notifications/unread-count').then((r) => r.data),
  list: (params) => api.get('/api/notifications', { params }).then((r) => r.data),
  read: (id) => api.put(`/api/notifications/${id}/read`),
  readAll: () => api.put('/api/notifications/read-all'),
}
