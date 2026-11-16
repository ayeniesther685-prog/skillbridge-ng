import { api } from './client';

export const providerVerificationApi = {
  getMine: () => api.get('/api/providers/me/verification'),
  submit: (payload) => api.post('/api/providers/me/verification/submit', payload),
  getStatus: (userId) => api.get(`/api/providers/${userId}/verification-status`),

  // admin
  adminList: (params) => api.get('/api/admin/provider-verifications', { params }),
  adminGet: (id) => api.get(`/api/admin/provider-verifications/${id}`),
  adminEvents: (id) => api.get(`/api/admin/provider-verifications/${id}/events`),
  adminApprove: (id, reason) => api.post(`/api/admin/provider-verifications/${id}/approve`, { reason }),
  adminReject: (id, reason) => api.post(`/api/admin/provider-verifications/${id}/reject`, { reason }),
  adminSuspend: (id, reason) => api.post(`/api/admin/provider-verifications/${id}/suspend`, { reason }),
  adminRevoke: (id, reason) => api.post(`/api/admin/provider-verifications/${id}/revoke`, { reason }),
};