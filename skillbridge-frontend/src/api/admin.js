import { api } from './client'
const d = (r) => r.data
export const adminApi = {
  createCourse: (body) => api.post('/api/admin/academy/courses', body).then(d),
  createLesson: ({ courseId, ...body }) => api.post(`/api/admin/academy/courses/${courseId}/lessons`, body).then(d),
  createAssignment: ({ courseId, ...body }) => api.post(`/api/admin/academy/courses/${courseId}/assignments`, body).then(d),
  submissions: (assignmentId) => api.get(`/api/admin/academy/assignments/${assignmentId}/submissions`).then(d),
  grade: ({ id, ...body }) => api.put(`/api/admin/submissions/${id}/grade`, body).then(d),
  grantQuota: ({ userId, amount }) => api.post(`/api/admin/ai/quota/${userId}/grant`, { amount }).then(d),
  setTier: ({ userId, tierName }) => api.put(`/api/admin/ai/quota/${userId}/tier`, { tierName }).then(d),
}
