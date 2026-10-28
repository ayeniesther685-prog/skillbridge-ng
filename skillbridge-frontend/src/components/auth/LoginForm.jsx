import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Mail } from 'lucide-react'
import { useAuth } from '../../context/AuthContext.jsx'
import { getErrorMessage } from '../../utils/errors.js'
import FormInput from './FormInput.jsx'
import PasswordInput from './PasswordInput.jsx'
import LoadingButton from './LoadingButton.jsx'
import AuthAlert from './AuthAlert.jsx'
import GoogleButton from './GoogleButton.jsx'

const schema = z.object({
  email: z.string().trim().min(1, 'Enter your email address.').email('Enter a valid email address.'),
  password: z.string().min(1, 'Enter your password.'),
  remember: z.boolean().optional(),
})

export default function LoginForm() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [serverError, setServerError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { email: '', password: '', remember: true },
  })

  const onSubmit = async (v) => {
    setServerError('')
    try {
      await login({ email: v.email, password: v.password, remember: v.remember })
      navigate(location.state?.from?.pathname ?? '/dashboard', { replace: true })
    } catch (err) {
      setServerError(getErrorMessage(err))
    }
  }

  return (
    <>
      <h2 className="text-2xl font-extrabold tracking-tight">Welcome back</h2>
      <p className="mt-1.5 text-sm text-muted">Sign in to continue to SkillBridge NG.</p>
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-7 space-y-4">
        <AuthAlert message={serverError} />
        <FormInput label="Email" icon={Mail} type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')} />
        <PasswordInput label="Password" autoComplete="current-password" placeholder="Enter your password" error={errors.password?.message} {...register('password')} />
        <div className="flex items-center justify-between text-sm">
          <label className="flex cursor-pointer items-center gap-2 text-slate-300">
            <input type="checkbox" className="h-4 w-4 rounded border-line bg-ink-800 accent-brand-500" {...register('remember')} />Remember me
          </label>
          <span className="text-muted" title="Password reset is not available yet">Forgot password?</span>
        </div>
        <LoadingButton type="submit" loading={isSubmitting}>{isSubmitting ? 'Signing in…' : 'Sign In'}</LoadingButton>
      </form>
      <div className="my-6 flex items-center gap-3 text-xs text-muted" aria-hidden="true"><span className="h-px flex-1 bg-line" />or<span className="h-px flex-1 bg-line" /></div>
      <GoogleButton />
      <p className="mt-7 text-center text-sm text-muted">Don't have an account? <Link to="/register" className="font-bold text-brand-400 hover:underline">Create one</Link></p>
    </>
  )
}
