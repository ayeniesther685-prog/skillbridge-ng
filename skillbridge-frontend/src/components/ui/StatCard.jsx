import Card from './Card'
export default function StatCard({ label, value, icon: Icon, loading }) {
  return (
    <Card className="p-5">
      <div className="flex items-center justify-between text-sm text-muted">{label}{Icon && <Icon className="h-4 w-4" />}</div>
      <div className="mt-2 text-2xl font-semibold">{loading ? <div className="skeleton h-7 w-16" /> : value}</div>
    </Card>
  )
}
