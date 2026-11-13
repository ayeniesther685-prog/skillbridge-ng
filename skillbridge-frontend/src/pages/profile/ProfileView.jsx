import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { profileApi } from '../../api/profile'
import { useAuth } from '../../context/AuthContext'
import Card, { CardBody } from '../../components/ui/Card'
import Avatar from '../../components/ui/Avatar'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { label } from '../../utils/format'

const SECTIONS = [['education', 'Education'], ['experience', 'Experience'], ['certifications', 'Certifications'], ['portfolio', 'Portfolio']]
export function ProfileBody({ userId, user }) {
  const profile = useQuery({ queryKey: ['profile', userId], queryFn: () => profileApi.publicProfile(userId) })
  const skills = useQuery({ queryKey: ['profile', 'skills', userId], queryFn: () => profileApi.list(userId, 'skills') })
  const sections = SECTIONS.map(([k]) => ({ k, q: useQuery({ queryKey: ['profile', k, userId], queryFn: () => profileApi.list(userId, k) }) })) // eslint-disable-line react-hooks/rules-of-hooks
  const arr = (d) => (Array.isArray(d) ? d : d?.content || [])
  return (
    <QueryBoundary q={profile}>{(p) => (
      <div className="space-y-4">
        <Card><CardBody className="flex flex-wrap items-center gap-4">
          <Avatar user={{ ...user, ...p }} size={56} />
          <div className="min-w-0 flex-1">
            <h2 className="text-lg font-semibold">{p.firstName ? `${p.firstName} ${p.lastName ?? ''}` : user ? `${user.firstName} ${user.lastName}` : `User #${userId}`}</h2>
            {p.headline && <p className="text-sm">{p.headline}</p>}
            {(p.location || user?.location) && <p className="text-sm text-muted">{p.location || user.location}</p>}
          </div>
          {p.hourlyRate != null && <Badge variant="info">₦{p.hourlyRate}/hr</Badge>}
        </CardBody></Card>
        {(p.bio || user?.bio) && <Card><CardBody><h3 className="mb-1 font-semibold">About</h3><p className="whitespace-pre-line text-sm text-muted">{p.bio || user.bio}</p></CardBody></Card>}
        <Card><CardBody><h3 className="mb-2 font-semibold">Skills</h3>
          <div className="flex flex-wrap gap-2">{arr(skills.data).map((s) => <Badge key={s.id} variant="info">{s.name}</Badge>)}{!skills.isLoading && arr(skills.data).length === 0 && <span className="text-sm text-muted">No skills listed.</span>}</div></CardBody></Card>
        {SECTIONS.map(([k, title], i) => (
          <Card key={k}><CardBody><h3 className="mb-2 font-semibold">{title}</h3>
            {arr(sections[i].q.data).length === 0 ? <p className="text-sm text-muted">Nothing added yet.</p> :
              <ul className="space-y-2 text-sm">{arr(sections[i].q.data).map((x) => <li key={x.id}><span className="font-medium">{x.title || x.name || x.institution || x.company}</span> <span className="text-muted">{x.degree || x.issuingOrganization || x.location || ''}</span></li>)}</ul>}
          </CardBody></Card>))}
        {Object.entries(p).some(([k, v]) => /url|link|github|linkedin|website|twitter/i.test(k) && v) && (
          <Card><CardBody><h3 className="mb-2 font-semibold">Links</h3><ul className="space-y-1 text-sm">{Object.entries(p).filter(([k, v]) => /url|link|github|linkedin|website|twitter/i.test(k) && v).map(([k, v]) => <li key={k}><a className="text-accent hover:underline" href={v} target="_blank" rel="noreferrer noopener">{label(k.replace(/([A-Z])/g, '_$1'))}</a></li>)}</ul></CardBody></Card>)}
      </div>)}
    </QueryBoundary>
  )
}
export default function ProfileView() {
  const { user } = useAuth()
  return <div><div className="mb-4 flex justify-end"><Button as={Link} to="/profile/edit" variant="outline" size="sm">Edit details</Button></div><ProfileBody userId={user.id} user={user} /></div>
}
