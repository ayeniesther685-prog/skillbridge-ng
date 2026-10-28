import { ExternalLink, FolderOpen } from 'lucide-react'
import ResourceManager from '../components/ResourceManager.jsx'
import ComingSoon from '../components/ComingSoon.jsx'
import { portfolioApi } from '../api/portfolioApi.js'

const fields = [
  { name: 'title', label: 'Title', required: true, max: 200 },
  { name: 'description', label: 'Description', type: 'textarea', max: 2000 },
  { name: 'imageUrl', label: 'Image URL', type: 'url', max: 500, placeholder: 'https://', hint: 'Paste a link — file uploads are not available yet.' },
  { name: 'projectUrl', label: 'Project URL', type: 'url', max: 500, placeholder: 'https://' },
  { name: 'technologies', label: 'Technologies', max: 500, placeholder: 'React, Spring Boot, PostgreSQL' },
]

export default function Portfolio() {
  return (
    <div className="space-y-6">
      <ResourceManager title="Portfolio" subtitle="Projects that show what you can do." noun="Project" icon={FolderOpen} api={portfolioApi} fields={fields}
        renderItem={(x) => (<><p className="font-extrabold">{x.title}</p>{x.technologies && <p className="text-xs font-semibold text-brand-400">{x.technologies}</p>}
          {x.description && <p className="mt-2 text-sm text-slate-300">{x.description}</p>}
          {x.projectUrl && <a href={x.projectUrl} target="_blank" rel="noreferrer noopener" className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-brand-400">Open project <ExternalLink className="h-3 w-3" /></a>}</>)} />
      <div className="max-w-sm"><ComingSoon title="File uploads" description="Upload images and documents directly. For now, add links." buttonLabel="Upload file" /></div>
    </div>
  )
}
