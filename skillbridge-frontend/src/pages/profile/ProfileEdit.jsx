import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import { profileApi } from '../../api/profile'
import { useSave } from '../../hooks/useApi'
import AutoForm from '../../components/ui/AutoForm'
import Card, { CardBody } from '../../components/ui/Card'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { optNum, optStr, optUrl } from '../../utils/validators'

const schema = z.object({ headline: optStr(), bio: optStr(), location: optStr(), phone: optStr(), hourlyRate: optNum(), profilePicture: optUrl(), githubUrl: optUrl(), linkedinUrl: optUrl(), websiteUrl: optUrl() })
const fields = [
  { name: 'headline', label: 'Headline', placeholder: 'Full-stack developer', wide: true },
  { name: 'bio', label: 'Bio', type: 'textarea' },
  { name: 'location', label: 'Location' },
  { name: 'hourlyRate', label: 'Hourly rate (₦)', type: 'number' },
  { name: 'profilePicture', label: 'Profile picture link', hint: 'Paste a link — file upload isn’t available yet.' },
  { name: 'githubUrl', label: 'GitHub link' },
  { name: 'linkedinUrl', label: 'LinkedIn link' },
  { name: 'websiteUrl', label: 'Website link' },
]
export default function ProfileEdit() {
  const q = useQuery({ queryKey: ['profile', 'me'], queryFn: profileApi.mine })
  const save = useSave(profileApi.update, { invalidate: [['profile']], success: 'Profile saved' })
  return (
    <QueryBoundary q={q}>{(p) => (
      <Card><CardBody>
        <AutoForm fields={fields} schema={schema} defaultValues={Object.fromEntries(fields.map((f) => [f.name, p?.[f.name] ?? '']))} onSubmit={(v) => save.mutateAsync(v)} submitLabel="Save profile" />
      </CardBody></Card>)}
    </QueryBoundary>
  )
}
