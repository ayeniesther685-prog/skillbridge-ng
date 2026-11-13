import { api } from './client'
const d = (r) => r.data
export const academyApi = {
  programs: () => api.get('/api/academy/programs').then(d),
  departments: (programId) => api.get('/api/academy/departments', { params: { programId } }).then(d),
  courses: (departmentId) => api.get('/api/academy/courses', { params: { departmentId } }).then(d),
  lessons: (courseId) => api.get(`/api/academy/courses/${courseId}/lessons`).then(d),
  lesson: (courseId, lessonId) => api.get(`/api/academy/courses/${courseId}/lessons/${lessonId}`).then(d),
  assignments: (courseId) => api.get(`/api/academy/courses/${courseId}/assignments`).then(d),
  assignment: (id) => api.get(`/api/academy/assignments/${id}`).then(d),
  record: () => api.get('/api/academy/me/record', { skipForbiddenRedirect: true }).then(d),
  enrollments: () => api.get('/api/academy/me/enrollments').then(d),
  enroll: (body) => api.post('/api/academy/enroll', body).then(d),
  enrollCourse: (body) => api.post('/api/academy/me/enrollments', body).then(d),
  setProgress: ({ lessonId, status }) => api.put(`/api/academy/lessons/${lessonId}/progress`, { status }).then(d),
  courseProgress: (courseId) => api.get(`/api/academy/me/courses/${courseId}/progress`).then(d),
  submit: ({ id, ...body }) => api.post(`/api/academy/assignments/${id}/submit`, body).then(d),
  mySubmission: (id) => api.get(`/api/academy/assignments/${id}/my-submission`).then(d),
  issueCertificate: (courseId) => api.post(`/api/academy/courses/${courseId}/certificate`).then(d),
  myCertificates: () => api.get('/api/academy/me/certificates').then(d),
}
