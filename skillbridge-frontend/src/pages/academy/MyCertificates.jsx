import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Award } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { academyApi } from '../../api/academy'
import { list } from './CoursePicker'
import { fmtDate } from '../../utils/format'
export default function MyCertificates() {
  const q = useQuery({ queryKey: ['certificates'], queryFn: academyApi.myCertificates })
  return (
    <div>
      <PageHeader title="My certificates" description="Anyone can verify these with the certificate number." />
      <QueryBoundary q={q}>{(d) => list(d).length === 0
        ? <EmptyState icon={Award} title="No certificates yet" description="Finish all lessons in a course to earn one." action={<Button as={Link} to="/academy/courses" size="sm">My courses</Button>} />
        : <div className="grid gap-3 sm:grid-cols-2">{list(d).map((c) => (
          <Card key={c.id ?? c.certificateNumber}><CardBody className="text-sm">
            <p className="font-semibold">{c.courseTitle || c.course?.title || 'Certificate'}</p>
            <p className="mt-1 font-mono text-xs text-muted">{c.certificateNumber}</p>
            <p className="mt-1 text-xs text-muted">Issued {fmtDate(c.issuedAt || c.createdAt)}</p>
            {c.certificateNumber && <Button as={Link} to={`/certificates/verify/${c.certificateNumber}`} variant="outline" size="sm" className="mt-3">View verification page</Button>}
          </CardBody></Card>))}</div>}</QueryBoundary>
    </div>
  )
}
