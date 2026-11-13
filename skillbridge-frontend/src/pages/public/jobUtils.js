import { naira } from '../../utils/format'
// DTO field names for budget are not confirmed — handle the common shapes.
export function jobBudget(j) {
  const min = j.budgetMin ?? j.minBudget, max = j.budgetMax ?? j.maxBudget
  if (min != null && max != null) return `${naira(min)} – ${naira(max)}`
  if (j.budget != null) return naira(j.budget)
  if (max != null) return `Up to ${naira(max)}`
  if (min != null) return `From ${naira(min)}`
  return null
}
