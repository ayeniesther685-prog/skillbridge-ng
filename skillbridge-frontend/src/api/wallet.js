import { api } from './client'
const d = (r) => r.data
export const walletApi = {
  me: () => api.get('/api/wallet/me').then(d),
  transactions: (params) => api.get('/api/wallet/me/transactions', { params }).then(d),
  withdraw: (body) => api.post('/api/wallet/withdraw', body).then(d),
  withdrawals: (params) => api.get('/api/wallet/me/withdrawals', { params }).then(d),
}
