export const formatDate = (v) =>
  v ? new Date(v.length === 10 ? `${v}T00:00:00` : v).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'

export const formatDateTime = (v) =>
  v ? new Date(v).toLocaleString('en-GB', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : ''

export function timeAgo(v) {
  if (!v) return ''
  const s = Math.max(1, Math.floor((Date.now() - new Date(v).getTime()) / 1000))
  const steps = [[60, 's'], [60, 'm'], [24, 'h'], [30, 'd'], [12, 'mo']]
  let n = s, unit = 's'
  for (const [div, u] of steps) { unit = u; if (n < div) return `${n}${u} ago`; n = Math.floor(n / div); unit = u }
  return `${n}${unit === 'mo' ? 'y' : unit} ago`
}

export const formatMoney = (n) =>
  n == null ? '—' : new Intl.NumberFormat('en-NG', { style: 'currency', currency: 'NGN', maximumFractionDigits: 0 }).format(n)

export const budgetRange = (min, max) =>
  min == null && max == null ? 'Budget not set' : min != null && max != null ? `${formatMoney(min)} – ${formatMoney(max)}` : formatMoney(min ?? max)

export const label = (s) => (s ? s.replaceAll('_', ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase()) : '')
