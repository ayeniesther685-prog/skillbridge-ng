import api, { unwrap } from './axios.js'

export const apply = (jobId, body) => unwrap(api.post(`/api/jobs/${jobId}/applications`, body))
export const mine = (params) => unwrap(api.get('/api/applications', { params }))
export const forJob = (jobId, params) => unwrap(api.get(`/api/jobs/${jobId}/applications`, { params }))
export const get = (id) => unwrap(api.get(`/api/applications/${id}`))
export const setStatus = (id, status) => unwrap(api.put(`/api/applications/${id}/status`, { status }))
export const withdraw = (id) => unwrap(api.put(`/api/applications/${id}/withdraw`))
export const remove = (id) => unwrap(api.delete(`/api/applications/${id}`))
