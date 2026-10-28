import api, { unwrap } from './axios.js'

export const list = (contractId) => unwrap(api.get(`/api/contracts/${contractId}/milestones`))
export const create = (contractId, body) => unwrap(api.post(`/api/contracts/${contractId}/milestones`, body))
export const update = (id, body) => unwrap(api.put(`/api/milestones/${id}`, body))
export const setStatus = (id, status) => unwrap(api.put(`/api/milestones/${id}/status`, { status }))
export const remove = (id) => unwrap(api.delete(`/api/milestones/${id}`))
