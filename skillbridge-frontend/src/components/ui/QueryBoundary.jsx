import { SkeletonList } from './Skeleton'
import ErrorState from './ErrorState'
// Renders skeleton / error / children(data) for a react-query result
export default function QueryBoundary({ q, rows = 4, children }) {
  if (q.isLoading) return <SkeletonList rows={rows} />
  if (q.isError) return <ErrorState error={q.error} onRetry={q.refetch} />
  return children(q.data)
}
