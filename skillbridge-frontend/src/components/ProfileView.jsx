import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Award, Briefcase, ExternalLink, Github, GraduationCap, Linkedin, Globe, MessageSquare, Star, Twitter } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { getPublicBundle } from '../api/userApi.js'
import * as reviewApi from '../api/reviewApi.js'
import * as messageApi from '../api/messageApi.js'
import { getErrorMessage } from '../utils/errors.js'
import { formatDate, formatMoney, label } from '../utils/dates.js'
import { useToast } from './Toast.jsx'
import { Button, Card } from './ui.jsx'
import SkillPill from './SkillPill.jsx'
import LoadingSpinner from './LoadingSpinner.jsx'

const Section = ({ icon: Icon, title, children, empty }) => (
  <Card>
    <h2 className="mb-3 flex items-center gap-2 text-base font-extrabold"><Icon className="h-4 w-4 text-brand-400" aria-hidden="true" />{title}</h2>
    {empty ? <p className="text-sm text-muted">{empty}</p> : children}
  </Card>
)
const range = (a, b, current) => `${a ? formatDate(a) : '—'} – ${current ? 'Present' : b ? formatDate(b) : '—'}`

/** Public profile for any user id. `basic` (name etc.) is only known for the signed-in user. */
export default function ProfileView({ userId, basic }) {
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [starting, setStarting] = useState(false)
  const { data, loading } = useFetch(() => getPublicBundle(userId), [userId])
  const { data: reviews } = useFetch(() => reviewApi.forUser(userId, { size: 5 }), [userId])

  if (loading || !data) return <LoadingSpinner />
  const { profile, skills, education, experience, certifications, portfolio, rating } = data
  const isSelf = user?.id === Number(userId)
  const links = [
    [Globe, profile?.website, 'Website'], [Linkedin, profile?.linkedinUrl, 'LinkedIn'],
    [Github, profile?.githubUrl, 'GitHub'], [Twitter, profile?.twitterUrl, 'Twitter'],
  ].filter(([, url]) => url)

  const message = async () => {
    setStarting(true)
    try { const c = await messageApi.startConversation(Number(userId)); navigate(`/messages/${c.id}`) }
    catch (e) { toast.error(getErrorMessage(e)) } finally { setStarting(false) }
  }

  return (
    <div className="space-y-5">
      <Card className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-4">
          <span className="grid h-16 w-16 shrink-0 place-items-center rounded-2xl bg-brand-500 text-xl font-extrabold">
            {basic ? `${basic.firstName?.[0] ?? ''}${basic.lastName?.[0] ?? ''}` : '#'}
          </span>
          <div className="min-w-0">
            <h1 className="text-2xl font-extrabold">{basic ? `${basic.firstName} ${basic.lastName}` : `Member #${userId}`}</h1>
            <p className="text-sm text-slate-300">{profile?.headline || (profile ? 'No headline yet' : 'No profile yet')}</p>
            {basic && <p className="mt-1 text-xs text-muted">{basic.email}{basic.location ? ` · ${basic.location}` : ''}{basic.roles?.length ? ` · ${basic.roles.map(label).join(', ')}` : ''}</p>}
            {basic?.bio && <p className="mt-2 max-w-xl text-sm text-slate-300">{basic.bio}</p>}
            <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm">
              {rating?.average != null && <span className="inline-flex items-center gap-1 font-bold text-amber-300"><Star className="h-4 w-4 fill-current" />{Number(rating.average).toFixed(1)}</span>}
              {profile?.availability && <span className="text-muted">Availability: <b className="text-slate-200">{label(profile.availability)}</b></span>}
              {profile?.hourlyRate != null && <span className="text-muted">Rate: <b className="text-slate-200">{formatMoney(profile.hourlyRate)}/hr</b></span>}
            </div>
            {links.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {links.map(([Icon, url, name]) => (
                  <a key={name} href={url} target="_blank" rel="noreferrer noopener" className="inline-flex items-center gap-1.5 rounded-lg border border-line px-2.5 py-1.5 text-xs font-semibold hover:bg-white/5"><Icon className="h-3.5 w-3.5" />{name}</a>
                ))}
              </div>
            )}
          </div>
        </div>
        {isAuthenticated && !isSelf && <Button onClick={message} loading={starting}><MessageSquare className="h-4 w-4" />Message</Button>}
      </Card>

      <Section icon={Star} title="Skills" empty={!skills.length && 'No skills added yet.'}>
        <div className="flex flex-wrap gap-2">{skills.map((s) => <SkillPill key={s.id} skill={s} />)}</div>
      </Section>
      <div className="grid gap-5 lg:grid-cols-2">
        <Section icon={Briefcase} title="Experience" empty={!experience.length && 'No experience added yet.'}>
          <ul className="space-y-4">{experience.map((x) => (
            <li key={x.id}><p className="font-bold">{x.position} · {x.company}</p><p className="text-xs text-muted">{range(x.startDate, x.endDate, x.current)}</p>{x.description && <p className="mt-1 text-sm text-slate-300">{x.description}</p>}</li>
          ))}</ul>
        </Section>
        <Section icon={GraduationCap} title="Education" empty={!education.length && 'No education added yet.'}>
          <ul className="space-y-4">{education.map((x) => (
            <li key={x.id}><p className="font-bold">{x.institution}</p><p className="text-sm text-slate-300">{[x.degree, x.fieldOfStudy].filter(Boolean).join(' · ')}</p><p className="text-xs text-muted">{range(x.startDate, x.endDate)}</p>{x.description && <p className="mt-1 text-sm text-slate-300">{x.description}</p>}</li>
          ))}</ul>
        </Section>
        <Section icon={Award} title="Certifications" empty={!certifications.length && 'No certifications added yet.'}>
          <ul className="space-y-3">{certifications.map((x) => (
            <li key={x.id}><p className="font-bold">{x.name}</p><p className="text-sm text-slate-300">{x.issuingOrganization}</p><p className="text-xs text-muted">Issued {formatDate(x.issueDate)}{x.expiryDate ? ` · Expires ${formatDate(x.expiryDate)}` : ''}</p>
              {x.credentialUrl && <a href={x.credentialUrl} target="_blank" rel="noreferrer noopener" className="inline-flex items-center gap-1 text-xs font-semibold text-brand-400">View credential <ExternalLink className="h-3 w-3" /></a>}</li>
          ))}</ul>
        </Section>
        <Section icon={Star} title="Reviews" empty={!reviews?.content?.length && 'No reviews yet.'}>
          <ul className="space-y-3">{reviews?.content?.map((r) => (
            <li key={r.id}><p className="text-sm font-bold">{'★'.repeat(r.rating)}<span className="text-slate-600">{'★'.repeat(5 - r.rating)}</span> <span className="font-normal text-muted">by {r.reviewerName} · {r.jobTitle}</span></p>{r.comment && <p className="text-sm text-slate-300">{r.comment}</p>}</li>
          ))}</ul>
        </Section>
      </div>
      <Section icon={Briefcase} title="Portfolio" empty={!portfolio.length && 'No portfolio items yet.'}>
        <div className="grid gap-4 md:grid-cols-2">{portfolio.map((p) => (
          <div key={p.id} className="rounded-xl border border-line bg-ink-800 p-4">
            <p className="font-bold">{p.title}</p>{p.technologies && <p className="text-xs text-brand-400">{p.technologies}</p>}
            {p.description && <p className="mt-1 text-sm text-slate-300">{p.description}</p>}
            {p.projectUrl && <a href={p.projectUrl} target="_blank" rel="noreferrer noopener" className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-brand-400">Open project <ExternalLink className="h-3 w-3" /></a>}
          </div>
        ))}</div>
      </Section>
      {isSelf && <p className="text-center text-xs text-muted">Manage sections: <Link className="text-brand-400" to="/skills">Skills</Link> · <Link className="text-brand-400" to="/experience">Experience</Link> · <Link className="text-brand-400" to="/education">Education</Link> · <Link className="text-brand-400" to="/certifications">Certifications</Link> · <Link className="text-brand-400" to="/portfolio">Portfolio</Link></p>}
    </div>
  )
}
