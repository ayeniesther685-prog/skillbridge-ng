import api, { unwrap } from './axios.js'

export const list = (params) => unwrap(api.get('/api/jobs', { params }))
export const get = (id) => unwrap(api.get(`/api/jobs/${id}`))
export const create = (body) => unwrap(api.post('/api/jobs', body))
export const update = (id, body) => unwrap(api.put(`/api/jobs/${id}`, body))
export const setStatus = (id, status) => unwrap(api.put(`/api/jobs/${id}/status`, { status }))
export const remove = (id) => unwrap(api.delete(`/api/jobs/${id}`))
