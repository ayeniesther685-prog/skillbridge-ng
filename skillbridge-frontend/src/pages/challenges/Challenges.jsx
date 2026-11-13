import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Trophy } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Card from '../../components/ui/Card'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { challengesApi } from '../../api/challenges'
import { useAuth } from '../../context/AuthContext'
import { list } from '../academy/CoursePicker'
export default function Challenges() {
  const { user } = useAuth()
  const q = useQuery({ queryKey: ['challenges'], queryFn: challengesApi.list })
  return (
    <div>
      <PageHeader title="Challenges" description="Solve challenges to earn points." actions={user && <Button as={Link} to="/challenges/me/submissions" variant="outline" size="sm">My submissions</Button>} />
      <QueryBoundary q={q}>{(d) => list(d).length === 0 ? <EmptyState icon={Trophy} title="No active challenges" description="Check back soon." /> :
        <div className="grid gap-3 sm:grid-cols-2">{list(d).map((c) => (
          <Link key={c.id} to={`/challenges/${c.id}`}><Card className="h-full p-4 hover:border-primary/60"><div className="flex items-start justify-between gap-2"><h3 className="font-semibold">{c.title}</h3>{(c.rewardPoints ?? c.points) != null && <Badge variant="success">{c.rewardPoints ?? c.points} pts</Badge>}</div>
            {c.description && <p className="mt-2 line-clamp-3 text-sm text-muted">{c.description}</p>}</Card></Link>))}</div>}</QueryBoundary>
    </div>
  )
}
