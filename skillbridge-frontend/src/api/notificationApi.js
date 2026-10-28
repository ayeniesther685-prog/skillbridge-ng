import api, { unwrap } from './axios.js'

export const list = (params) => unwrap(api.get('/api/notifications', { params }))
export const unreadCount = () => unwrap(api.get('/api/notifications/unread-count'))
export const markRead = (id) => unwrap(api.put(`/api/notifications/${id}/read`))
export const markAllRead = () => unwrap(api.put('/api/notifications/read-all'))
