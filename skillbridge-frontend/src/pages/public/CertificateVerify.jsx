import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { BadgeCheck, XCircle } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import Card, { CardBody } from '../../components/ui/Card'
import Skeleton from '../../components/ui/Skeleton'
import { certificatesApi } from '../../api/certificates'
import { normalizeError } from '../../api/errors'
import { fmtDate } from '../../utils/format'

export default function CertificateVerify() {
  const { num } = useParams()
  const nav = useNavigate()
  const [value, setValue] = useState(num || '')
  const q = useQuery({ queryKey: ['cert', num], queryFn: () => certificatesApi.verify(num), enabled: !!num, retry: false })
  const submit = (e) => { e.preventDefault(); if (value.trim()) nav(`/certificates/verify/${encodeURIComponent(value.trim())}`) }
  return (
    <div className="max-w-xl">
      <PageHeader title="Verify a certificate" description="Enter the certificate number to confirm it was issued by SkillBridge NG." />
      <form onSubmit={submit} className="flex items-end gap-2"><Input className="flex-1" label="Certificate number" value={value} onChange={(e) => setValue(e.target.value)} /><Button type="submit">Verify</Button></form>
      <div className="mt-6">
        {q.isLoading && <Skeleton variant="card" />}
        {q.isError && (
          <Card className="border-danger/40"><CardBody className="flex items-start gap-3"><XCircle className="mt-0.5 h-5 w-5 text-danger" />
            <div><p className="font-semibold">{normalizeError(q.error).status === 404 ? 'No certificate found' : 'Could not verify'}</p><p className="text-sm text-muted">{normalizeError(q.error).status === 404 ? 'Check the number and try again.' : normalizeError(q.error).message}</p></div></CardBody></Card>)}
        {q.data && (
          <Card className="border-success/40"><CardBody>
            <div className="flex items-center gap-2 font-semibold text-success"><BadgeCheck className="h-5 w-5" />Valid certificate</div>
            <dl className="mt-4 space-y-2 text-sm">
              {Object.entries(q.data).filter(([, v]) => v != null && typeof v !== 'object').map(([k, v]) => (
                <div key={k} className="flex justify-between gap-4"><dt className="text-muted">{k.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase())}</dt><dd className="text-right">{/date|At$/i.test(k) ? fmtDate(v) : String(v)}</dd></div>))}
            </dl>
          </CardBody></Card>)}
      </div>
    </div>
  )
}
