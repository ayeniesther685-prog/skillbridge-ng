export const naira = (n) => (n == null ? '—' : new Intl.NumberFormat('en-NG', { style: 'currency', currency: 'NGN', maximumFractionDigits: 0 }).format(n))
export const fmtDate = (d) => (d ? new Intl.DateTimeFormat('en-NG', { dateStyle: 'medium' }).format(new Date(d)) : '—')
export const initials = (u) => `${u?.firstName?.[0] ?? ''}${u?.lastName?.[0] ?? ''}`.toUpperCase() || '?'
export const label = (s) => (s ? s.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase()) : '')
