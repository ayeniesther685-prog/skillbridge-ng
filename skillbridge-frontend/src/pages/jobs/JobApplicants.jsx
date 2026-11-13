import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { Users } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import DataTable from '../../components/ui/DataTable'
import StatusBadge from '../../components/ui/StatusBadge'
import Pagination from '../../components/ui/Pagination'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Button from '../../components/ui/Button'
import Dialog from '../../components/ui/Dialog'
import AutoForm from '../../components/ui/AutoForm'
import { applicationsApi } from '../../api/applications'
import { contractsApi } from '../../api/contracts'
import { usePaged, useSave } from '../../hooks/useApi'
import { nameOf } from '../../utils/ids'
import { naira, fmtDate } from '../../utils/format'
import { optNum, optStr } from '../../utils/validators'

export const applicantOf = (a) => a.applicant || a.freelancer || a.user
export default function JobApplicants() {
  const { id } = useParams()
  const [contractFor, setContractFor] = useState(null)
  const { q, setPage } = usePaged(['applications', 'job', id], (p) => applicationsApi.forJob(id, p))
  const setStatus = useSave(applicationsApi.setStatus, { invalidate: [['applications']], success: 'Application updated' })
  const createContract = useSave(contractsApi.create, { invalidate: [['contracts']], success: 'Contract created', onSuccess: () => setContractFor(null) })
  const cols = [
    { key: 'who', header: 'Applicant', render: (a) => { const u = applicantOf(a); const uid = u?.id ?? a.applicantId ?? a.userId; return uid ? <Link className="text-accent hover:underline" to={`/freelancers/${uid}`}>{nameOf(u) || `User #${uid}`}</Link> : '—' } },
    { key: 'proposal', header: 'Proposal', render: (a) => <span className="line-clamp-2 max-w-md">{a.coverLetter || a.proposal || a.message || '—'}</span> },
    { key: 'amount', header: 'Proposed', render: (a) => naira(a.proposedAmount ?? a.proposedRate ?? a.bidAmount) },
    { key: 'createdAt', header: 'Applied', render: (a) => fmtDate(a.createdAt || a.appliedAt) },
    { key: 'status', header: 'Status', render: (a) => <StatusBadge status={a.status} /> },
    { key: 'act', header: '', render: (a) => (
      <div className="flex flex-wrap justify-end gap-2" onClick={(e) => e.stopPropagation()}>
        {a.status === 'PENDING' && <><Button size="sm" onClick={() => setStatus.mutate({ id: a.id, status: 'ACCEPTED' })} disabled={setStatus.isPending}>Accept</Button>
          <Button size="sm" variant="outline" onClick={() => setStatus.mutate({ id: a.id, status: 'REJECTED' })} disabled={setStatus.isPending}>Reject</Button></>}
        {a.status === 'ACCEPTED' && <Button size="sm" onClick={() => setContractFor(a)}>Create contract</Button>}
      </div>) },
  ]
  return (
    <div>
      <Breadcrumbs items={[['Jobs', '/jobs'], ['Job', `/jobs/${id}`], ['Applicants']]} />
      <PageHeader title="Applicants" description="Accept or reject applications, then create a contract with the person you choose." />
      <QueryBoundary q={q}>{(d) => d.content.length === 0
        ? <EmptyState icon={Users} title="No applications yet" description="Applications show here as freelancers apply." />
        : <><DataTable columns={cols} rows={d.content} /><Pagination page={d} onPage={setPage} /></>}</QueryBoundary>
      <Dialog open={!!contractFor} onClose={() => setContractFor(null)} title="Create contract">
        {contractFor && <AutoForm schema={z.object({ totalAmount: optNum(), terms: optStr() })} defaultValues={{ totalAmount: '', terms: '' }}
          fields={[{ name: 'totalAmount', label: 'Total amount (₦)', type: 'number' }, { name: 'terms', label: 'Terms', type: 'textarea' }]}
          onSubmit={(v) => createContract.mutateAsync({ applicationId: contractFor.id, ...v })} submitLabel="Create contract" onCancel={() => setContractFor(null)} />}
      </Dialog>
    </div>
  )
}
