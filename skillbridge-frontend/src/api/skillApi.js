import api, { unwrap } from './axios.js'

export const list = (category) => unwrap(api.get('/api/skills', { params: category ? { category } : undefined }))
export const mine = () => unwrap(api.get('/api/users/me/skills'))
export const ofUser = (userId) => unwrap(api.get(`/api/users/${userId}/skills`))
export const attach = (skillId) => unwrap(api.post(`/api/users/me/skills/${skillId}`))
export const detach = (skillId) => unwrap(api.delete(`/api/users/me/skills/${skillId}`))
