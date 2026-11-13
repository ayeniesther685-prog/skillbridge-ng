import { api } from './client'
const d = (r) => r.data
export const reviewsApi = {
  create: (body) => api.post('/api/reviews', body).then(d),
  mine: (params) => api.get('/api/reviews/mine', { params }).then(d),
  forUser: (userId, params) => api.get(`/api/reviews/user/${userId}`, { params }).then(d),
  rating: (userId) => api.get(`/api/reviews/user/${userId}/rating`).then(d),
}
