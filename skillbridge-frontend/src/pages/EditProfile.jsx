import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import * as profileApi from '../api/profileApi.js'
import { getErrorMessage } from '../utils/errors.js'
import { Button, Card, Field, PageHeader, inputCls } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'

const blank = { headline: '', website: '', linkedinUrl: '', githubUrl: '', twitterUrl: '', availability: '', hourlyRate: '' }

export default function EditProfile() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [f, setF] = useState(blank)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    profileApi.getMyProfile()
      .then((p) => setF(Object.fromEntries(Object.keys(blank).map((k) => [k, p[k] ?? '']))))
      .catch((e) => { if (e.response?.status !== 404) setError(getErrorMessage(e)) }) // 404 = no profile yet
      .finally(() => setLoading(false))
  }, [])

  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    if (saving) return
    const rate = f.hourlyRate === '' ? null : Number(f.hourlyRate)
    if (rate != null && (Number.isNaN(rate) || rate < 0)) return setError('Hourly rate must be a positive number.')
    setSaving(true); setError('')
    try {
      await profileApi.updateMyProfile({
        headline: f.headline.trim() || null, website: f.website.trim() || null, linkedinUrl: f.linkedinUrl.trim() || null,
        githubUrl: f.githubUrl.trim() || null, twitterUrl: f.twitterUrl.trim() || null,
        availability: f.availability.trim() || null, hourlyRate: rate,
      })
      toast.success('Profile updated.')
      navigate('/profile')
    } catch (err) { setError(getErrorMessage(err)) } finally { setSaving(false) }
  }

  if (loading) return <LoadingSpinner />
  return (
    <div className="mx-auto max-w-2xl">
      <PageHeader title="Edit profile" subtitle="Your name, email, bio and location are read-only for now." />
      <Card className="mb-5 grid gap-1 text-sm sm:grid-cols-2">
        <p><span className="text-muted">Name: </span>{user.firstName} {user.lastName}</p>
        <p><span className="text-muted">Email: </span>{user.email}</p>
        <p><span className="text-muted">Phone: </span>{user.phone || '—'}</p>
        <p><span className="text-muted">Location: </span>{user.location || '—'}</p>
      </Card>
      <Card>
        <form onSubmit={submit} noValidate className="space-y-4">
          {error && <ErrorMessage message={error} />}
          <Field label="Headline"><input className={inputCls} value={f.headline} onChange={set('headline')} placeholder="Backend Developer" /></Field>
          <Field label="Website"><input className={inputCls} type="url" value={f.website} onChange={set('website')} placeholder="https://" /></Field>
          <Field label="LinkedIn URL"><input className={inputCls} type="url" value={f.linkedinUrl} onChange={set('linkedinUrl')} placeholder="https://linkedin.com/in/…" /></Field>
          <Field label="GitHub URL"><input className={inputCls} type="url" value={f.githubUrl} onChange={set('githubUrl')} placeholder="https://github.com/…" /></Field>
          <Field label="Twitter URL"><input className={inputCls} type="url" value={f.twitterUrl} onChange={set('twitterUrl')} placeholder="https://twitter.com/…" /></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Availability" hint="e.g. OPEN"><input className={inputCls} value={f.availability} onChange={set('availability')} placeholder="OPEN" /></Field>
            <Field label="Hourly rate (₦)"><input className={inputCls} type="number" min="0" step="0.01" value={f.hourlyRate} onChange={set('hourlyRate')} /></Field>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => navigate('/profile')} disabled={saving}>Cancel</Button>
            <Button type="submit" loading={saving}>{saving ? 'Saving…' : 'Save changes'}</Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
