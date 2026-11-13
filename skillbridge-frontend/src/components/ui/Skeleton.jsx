export default function Skeleton({ variant = 'text', className = '' }) {
  const s = { text: 'h-4 w-full', card: 'h-28 w-full', list: 'h-14 w-full' }[variant]
  return <div className={`skeleton ${s} ${className}`} aria-hidden />
}
export const SkeletonList = ({ rows = 5 }) => <div className="space-y-3">{Array.from({ length: rows }, (_, i) => <Skeleton key={i} variant="list" />)}</div>
