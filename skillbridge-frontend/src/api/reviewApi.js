import api, { unwrap } from './axios.js'

export const create = (body) => unwrap(api.post('/api/reviews', body))
export const mine = (params) => unwrap(api.get('/api/reviews/mine', { params }))
export const forUser = (userId, params) => unwrap(api.get(`/api/reviews/user/${userId}`, { params }))
export const forJob = (jobId, params) => unwrap(api.get(`/api/reviews/job/${jobId}`, { params }))
export const rating = (userId) => unwrap(api.get(`/api/reviews/user/${userId}/rating`))
