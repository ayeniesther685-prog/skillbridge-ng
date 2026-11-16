import { api } from './client';

const d = (r) => r.data;

export const disputesApi = {
  // ---------- user ----------
  open: ({ contractId, milestoneId, reason }) =>
    api.post(`/api/contracts/${contractId}/disputes`, { milestoneId, reason }).then(d),
  listForContract: (contractId) =>
    api.get(`/api/contracts/${contractId}/disputes`).then(d),
  get: (disputeId) =>
    api.get(`/api/disputes/${disputeId}`).then(d),
  addMessage: (disputeId, message) =>
    api.post(`/api/disputes/${disputeId}/messages`, { message }).then(d),
  addEvidence: (disputeId, { evidenceType, url, description }) =>
    api.post(`/api/disputes/${disputeId}/evidence`, { evidenceType, url, description }).then(d),
  withdraw: (disputeId) =>
    api.post(`/api/disputes/${disputeId}/withdraw`).then(d),

  // ---------- admin ----------
  adminList: (params) =>
    api.get('/api/admin/disputes', { params }).then(d),
  adminAddMessage: (disputeId, message) =>
    api.post(`/api/admin/disputes/${disputeId}/messages`, { message }).then(d),
  adminResolve: (disputeId, { decision, resolution }) =>
    api.post(`/api/admin/disputes/${disputeId}/resolve`, { decision, resolution }).then(d),
};
