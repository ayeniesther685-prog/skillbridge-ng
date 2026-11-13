import { api } from './client'
const d = (r) => r.data
export const aiApi = {
  quota: () => api.get('/api/ai/quota').then(d),
  usage: (params) => api.get('/api/ai/usage', { params }).then(d),
  tiers: () => api.get('/api/ai/tiers').then(d),
  tutor: (message) => api.post('/api/ai/tutor', { message }).then(d),
}
