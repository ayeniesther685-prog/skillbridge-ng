import { useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, Navigate } from 'react-router-dom'
import AuthShell from './AuthShell'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { registerSchema } from '../../utils/validators'
import { useAuth } from '../../context/AuthContext'
import { normalizeError } from '../../api/errors'
import { useToast } from '../../context/ToastContext'
import Spinner from '../../components/ui/Spinner'
import { authApi } from '../../api/auth'

const ROLES = [
  ['STUDENT', 'Student', 'Learn in the Academy'],
  ['FREELANCER', 'Freelancer', 'Find work and get paid'],
  ['CLIENT', 'Client', 'Post jobs and hire'],
]

export default function Register() {
  const { register: signUp, user, restoring } = useAuth()
  const toast = useToast()
  const { register, control, handleSubmit, setError, formState: { errors, isSubmitting } } =
    useForm({ resolver: zodResolver(registerSchema), defaultValues: { roles: ['STUDENT'] } })
  const [successEmail, setSuccessEmail] = useState(null)
  const [resending, setResending] = useState(false)

  if (restoring) return <Spinner full />
  if (user) return <Navigate to="/dashboard" replace />
  if (successEmail) {
    return (
      <AuthShell title="Check your email" subtitle="One more step to activate your account." footer={<>Wrong email? <Link to="/register" className="text-accent hover:underline">Start over</Link></>}>
        <div className="space-y-4 text-sm">
          <p className="text-slate-300">
            We've sent a verification link to <strong>{successEmail}</strong>.
          </p>
          <p className="text-muted">
            Click the link in the email to activate your account. If you don't see it within a few minutes, check your spam folder.
          </p>
          <button
            type="button"
            onClick={async () => {
              setResending(true)
              try {
                await authApi.resendVerification(successEmail)
                toast.success('Verification email resent.')
              } catch {
                toast.error('Could not resend. Try again in a moment.')
              } finally {
                setResending(false)
              }
            }}
            disabled={resending}
            className="text-indigo-400 hover:underline disabled:opacity-50"
          >
            {resending ? 'Sending…' : 'Resend verification email'}
          </button>
        </div>
      </AuthShell>
    )
  }

  const onSubmit = async (v) => {
    const body = { ...v, phone: v.phone?.trim() || undefined }
    try {
      const u = await signUp(body)
      // Phase 3A: unverified users cannot log in. Show "check your email" instead of
      // redirecting to dashboard. Backend already sent the verification email.
      setSuccessEmail(u.user?.email || v.email)
    } catch (err) {
      const e = normalizeError(err)
      if (e.fieldErrors) Object.entries(e.fieldErrors).forEach(([k, m]) => setError(k, { message: m }))
      else if (e.status === 409) setError('email', { message: 'An account with this email already exists.' })
      else toast.error(e.message)
    }
  }
  return (
    <AuthShell title="Create your account" subtitle="Choose the roles that fit what you want to do." footer={<>Already registered? <Link to="/login" className="text-accent hover:underline">Log in</Link></>}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="grid grid-cols-2 gap-3">
          <Input label="First name" autoComplete="given-name" error={errors.firstName?.message} {...register('firstName')} />
          <Input label="Last name" autoComplete="family-name" error={errors.lastName?.message} {...register('lastName')} />
        </div>
        <Input label="Email" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
        <Input label="Phone (optional)" type="tel" autoComplete="tel" error={errors.phone?.message} {...register('phone')} />
        <Input label="Password" type="password" autoComplete="new-password" hint="At least 8 characters." error={errors.password?.message} {...register('password')} />
        <Controller name="roles" control={control} render={({ field }) => (
          <fieldset>
            <legend className="mb-1.5 text-sm font-medium">I want to</legend>
            <div className="space-y-2">
              {ROLES.map(([value, name, desc]) => {
                const checked = field.value.includes(value)
                return (
                  <label key={value} className={`flex cursor-pointer items-center gap-3 rounded border p-3 ${checked ? 'border-primary bg-primary/10' : ''}`}>
                    <input type="checkbox" checked={checked} onChange={() => field.onChange(checked ? field.value.filter((r) => r !== value) : [...field.value, value])} className="h-4 w-4 accent-[rgb(var(--primary))]" />
                    <span><span className="block text-sm font-medium">{name}</span><span className="block text-xs text-muted">{desc}</span></span>
                  </label>
                )
              })}
            </div>
            {errors.roles && <p className="mt-1 text-xs text-danger">{errors.roles.message}</p>}
          </fieldset>
        )} />
        <Button type="submit" className="w-full" loading={isSubmitting}>Create account</Button>
      </form>
    </AuthShell>
  )
}
