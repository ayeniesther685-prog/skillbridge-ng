import api, { unwrap } from './axios.js'

export const getMyProfile = () => unwrap(api.get('/api/users/me/profile'))
export const updateMyProfile = (body) => unwrap(api.put('/api/users/me/profile', body))
export const getUserProfile = (userId) => unwrap(api.get(`/api/users/${userId}/profile`))
