import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import AuthShell from './AuthShell'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { loginSchema } from '../../utils/validators'
import { useAuth, dashboardFor, primaryRole } from '../../context/AuthContext'
import { normalizeError } from '../../api/errors'
import { useToast } from '../../context/ToastContext'
import Spinner from '../../components/ui/Spinner'
import { authApi } from '../../api/auth'

export default function Login() {
  const { login, user, restoring } = useAuth()
  const nav = useNavigate()
  const [sp] = useSearchParams()
  const toast = useToast()
  const next = sp.get('next')
  const safeNext = next && next.startsWith('/') && !next.startsWith('//') ? next : null
  const { register, handleSubmit, setError, getValues, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(loginSchema) })
  const [unverifiedEmail, setUnverifiedEmail] = useState(null)
  const [resending, setResending] = useState(false)

  if (restoring) return <Spinner full />
  if (user) return <Navigate to={safeNext || dashboardFor(primaryRole(user.roles))} replace />

  const onSubmit = async (values) => {
    try {
      const u = await login(values)
      nav(safeNext || dashboardFor(primaryRole(u.roles)), { replace: true })
    } catch (err) {
      const e = normalizeError(err)
      if (e.fieldErrors) Object.entries(e.fieldErrors).forEach(([k, m]) => setError(k, { message: m }))
      else if (e.status === 403 && err?.response?.data?.error === 'Email Not Verified') {
        setUnverifiedEmail(values.email)
      }
      else if (e.status === 401) setError('password', { message: 'Email or password is incorrect.' })
      else toast.error(e.message)
    }
  }

  const handleResend = async () => {
    setResending(true)
    try {
      await authApi.resendVerification(unverifiedEmail)
      toast.success('Verification email sent. Check your inbox.')
      setUnverifiedEmail(null)
    } catch {
      toast.error('Could not resend. Try again in a moment.')
    } finally {
      setResending(false)
    }
  }

  return (
    <AuthShell title="Log in" subtitle="Pick up where you left off." footer={<>New here? <Link to="/register" className="text-accent hover:underline">Create an account</Link></>}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <Input label="Email" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
        <Input label="Password" type="password" autoComplete="current-password" error={errors.password?.message} {...register('password')} />
        <Button type="submit" className="w-full" loading={isSubmitting}>Log in</Button>
      </form>

      {unverifiedEmail && (
        <div className="mt-4 rounded border border-amber-500/30 bg-amber-500/10 p-3 text-sm">
          <p className="text-amber-300 mb-2">
            Your email <strong>{unverifiedEmail}</strong> is not verified yet. Check your inbox for the verification link.
          </p>
          <button
            type="button"
            onClick={handleResend}
            disabled={resending}
            className="text-indigo-400 hover:underline disabled:opacity-50"
          >
            {resending ? 'Sending…' : 'Resend verification email'}
          </button>
        </div>
      )}

      <p className="mt-4 text-xs text-muted">Password reset isn't available yet — it's planned for v2.</p>
    </AuthShell>
  )
}
