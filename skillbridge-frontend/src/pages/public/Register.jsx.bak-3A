import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import AuthShell from './AuthShell'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { registerSchema } from '../../utils/validators'
import { useAuth, dashboardFor, primaryRole } from '../../context/AuthContext'
import { normalizeError } from '../../api/errors'
import { useToast } from '../../context/ToastContext'
import Spinner from '../../components/ui/Spinner'

const ROLES = [
  ['STUDENT', 'Student', 'Learn in the Academy'],
  ['FREELANCER', 'Freelancer', 'Find work and get paid'],
  ['CLIENT', 'Client', 'Post jobs and hire'],
]

export default function Register() {
  const { register: signUp, user, restoring } = useAuth()
  const nav = useNavigate()
  const toast = useToast()
  const { register, control, handleSubmit, setError, formState: { errors, isSubmitting } } =
    useForm({ resolver: zodResolver(registerSchema), defaultValues: { roles: ['STUDENT'] } })

  if (restoring) return <Spinner full />
  if (user) return <Navigate to={dashboardFor(primaryRole(user.roles))} replace />

  const onSubmit = async (v) => {
    const body = { ...v, phone: v.phone?.trim() || undefined }
    try {
      const u = await signUp(body)
      nav(dashboardFor(primaryRole(u.roles)), { replace: true })
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
