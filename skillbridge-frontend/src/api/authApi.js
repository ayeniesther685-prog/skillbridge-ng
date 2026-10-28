import api, { unwrap } from './axios.js'

export const register = (body) => unwrap(api.post('/api/auth/register', body))
export const login = (body) => unwrap(api.post('/api/auth/login', body))
export const logout = (refreshToken) => unwrap(api.post('/api/auth/logout', { refreshToken }))
export const me = () => unwrap(api.get('/api/auth/me'))
