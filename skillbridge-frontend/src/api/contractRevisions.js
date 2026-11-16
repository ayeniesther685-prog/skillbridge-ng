import { api } from './client';

export const contractRevisionsApi = {
  // CLIENT
  requestRevision: (milestoneId, payload) =>
    api.post(`/api/milestones/${milestoneId}/revisions`, payload),

  // FREELANCER
  respond: (revisionId, payload) =>
    api.post(`/api/revisions/${revisionId}/respond`, payload),

  // BOTH
  listForContract: (contractId) =>
    api.get(`/api/contracts/${contractId}/revisions`),
  listForMilestone: (milestoneId) =>
    api.get(`/api/milestones/${milestoneId}/revisions`),
  getOne: (revisionId) =>
    api.get(`/api/revisions/${revisionId}`),
};
