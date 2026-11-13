import { api } from './client'
export const certificatesApi = {
  verify: (num) => api.get(`/api/certificates/verify/${encodeURIComponent(num)}`).then((r) => r.data),
}
