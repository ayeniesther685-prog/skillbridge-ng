import api, { unwrap } from './axios.js'

export const create = (applicationId) => unwrap(api.post('/api/contracts', { applicationId }))
export const list = (params) => unwrap(api.get('/api/contracts', { params }))
export const get = (id) => unwrap(api.get(`/api/contracts/${id}`))
export const setStatus = (id, status) => unwrap(api.put(`/api/contracts/${id}/status`, { status }))
