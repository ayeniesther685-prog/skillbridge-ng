import api, { unwrap } from './axios.js'

export const conversations = (params) => unwrap(api.get('/api/conversations', { params }))
export const startConversation = (otherUserId) => unwrap(api.post('/api/conversations', { otherUserId }))
export const getConversation = (id) => unwrap(api.get(`/api/conversations/${id}`))
export const messages = (id, params) => unwrap(api.get(`/api/conversations/${id}/messages`, { params }))
export const send = (id, content) => unwrap(api.post(`/api/conversations/${id}/messages`, { content }))
export const markRead = (messageId) => unwrap(api.put(`/api/messages/${messageId}/read`))
