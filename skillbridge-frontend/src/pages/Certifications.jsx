import { Award, ExternalLink } from 'lucide-react'
import ResourceManager from '../components/ResourceManager.jsx'
import { certificationApi } from '../api/certificationApi.js'
import { formatDate } from '../utils/dates.js'

const fields = [
  { name: 'name', label: 'Certification name', required: true, max: 200 },
  { name: 'issuingOrganization', label: 'Issuing organization', max: 200 },
  { name: 'issueDate', label: 'Issue date', type: 'date' },
  { name: 'expiryDate', label: 'Expiry date', type: 'date' },
  { name: 'credentialUrl', label: 'Credential URL', type: 'url', max: 400, placeholder: 'https://' },
]

export default function Certifications() {
  return (
    <ResourceManager title="Certifications" subtitle="Credentials that back up your skills." noun="Certification" icon={Award} api={certificationApi} fields={fields}
      renderItem={(x) => (<><p className="font-extrabold">{x.name}</p><p className="text-sm text-slate-300">{x.issuingOrganization}</p>
        <p className="text-xs text-muted">Issued {formatDate(x.issueDate)}{x.expiryDate ? ` · Expires ${formatDate(x.expiryDate)}` : ''}</p>
        {x.credentialUrl && <a href={x.credentialUrl} target="_blank" rel="noreferrer noopener" className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-brand-400">View credential <ExternalLink className="h-3 w-3" /></a>}</>)} />
  )
}
