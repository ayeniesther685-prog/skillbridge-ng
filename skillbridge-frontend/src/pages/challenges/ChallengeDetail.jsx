import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import Button from '../../components/ui/Button'
import StatusBadge from '../../components/ui/StatusBadge'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { challengesApi } from '../../api/challenges'
import { useAuth } from '../../context/AuthContext'
import { useSave } from '../../hooks/useApi'
import { reqStr } from '../../utils/validators'
import { list } from '../academy/CoursePicker'
export default function ChallengeDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const q = useQuery({ queryKey: ['challenge', id], queryFn: () => challengesApi.get(id) })
  const mine = useQuery({ queryKey: ['challenges', 'mine'], queryFn: challengesApi.mine, enabled: !!user })
  const submit = useSave(challengesApi.submit, { invalidate: [['challenges', 'mine'], ['wallet']], success: 'Submission sent' })
  const prior = list(mine.data).filter((s) => String(s.challengeId ?? s.challenge?.id) === id)
  return (
    <QueryBoundary q={q}>{(c) => (
      <div className="max-w-2xl">
        <Breadcrumbs items={[['Challenges', '/challenges'], [c.title]]} />
        <PageHeader title={c.title} />
        <Card className="mb-4"><CardBody className="whitespace-pre-line text-sm">{c.description}</CardBody></Card>
        {prior.length > 0 && <Card className="mb-4"><CardBody className="space-y-2 text-sm"><h3 className="font-semibold">Your status</h3>{prior.map((s) => <div key={s.id} className="flex items-center justify-between"><span className="line-clamp-1 text-muted">{s.content}</span><StatusBadge status={s.status} /></div>)}</CardBody></Card>}
        {user ? <Card><CardBody><AutoForm schema={z.object({ content: reqStr('Write your solution') })} defaultValues={{ content: '' }} fields={[{ name: 'content', label: 'Your solution', type: 'textarea' }]} onSubmit={(v) => submit.mutateAsync({ id, ...v })} submitLabel="Submit solution" /></CardBody></Card>
          : <Button as={Link} to={`/login?next=/challenges/${id}`}>Log in to submit</Button>}
      </div>)}
    </QueryBoundary>
  )
}
