import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Mail, Phone, UserRound } from 'lucide-react'
import { useAuth } from '../../context/AuthContext.jsx'
import { getErrorMessage } from '../../services/authService.js'
import FormInput from './FormInput.jsx'
import PasswordInput from './PasswordInput.jsx'
import PasswordStrength from './PasswordStrength.jsx'
import RoleSelect from './RoleSelect.jsx'
import LoadingButton from './LoadingButton.jsx'
import AuthAlert from './AuthAlert.jsx'

// Limits mirror backend RegisterRequest: names <=100, email <=180, password 8..100, phone <=30
const schema = z
  .object({
    firstName: z.string().trim().min(1, 'Enter your first name.').max(100, 'First name is too long.'),
    lastName: z.string().trim().min(1, 'Enter your last name.').max(100, 'Last name is too long.'),
    email: z.string().trim().min(1, 'Enter your email address.').email('Enter a valid email address.').max(180, 'Email is too long.'),
    phone: z.string().trim().max(30, 'Phone number is too long.').optional(),
    roles: z.array(z.enum(['STUDENT', 'FREELANCER', 'CLIENT'])).min(1, 'Choose at least one option.'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters.')
      .max(100, 'Password must be 100 characters or fewer.')
      .regex(/[A-Z]/, 'Include at least one uppercase letter.')
      .regex(/[a-z]/, 'Include at least one lowercase letter.')
      .regex(/\d/, 'Include at least one number.'),
    confirmPassword: z.string().min(1, 'Confirm your password.'),
    terms: z.boolean().refine((v) => v === true, 'You must agree to continue.'),
  })
  .refine((d) => d.password === d.confirmPassword, { path: ['confirmPassword'], message: 'Passwords do not match.' })

export default function RegisterForm() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()
  const [serverError, setServerError] = useState('')
  const [success, setSuccess] = useState('')
  const { register, control, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { firstName: '', lastName: '', email: '', phone: '', roles: [], password: '', confirmPassword: '', terms: false },
  })

  const onSubmit = async (v) => {
    setServerError('')
    try {
      await registerUser({
        firstName: v.firstName, lastName: v.lastName, email: v.email,
        password: v.password, phone: v.phone, roles: v.roles,
      })
      setSuccess('Account created successfully. Welcome to SkillBridge!')
      setTimeout(() => navigate('/dashboard', { replace: true }), 1000)
    } catch (err) {
      setServerError(getErrorMessage(err, 'register'))
    }
  }

  return (
    <>
      <h2 className="text-2xl font-extrabold tracking-tight">Create your SkillBridge account</h2>
      <p className="mt-1.5 text-sm text-muted">Join the marketplace for skills, jobs and opportunities.</p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-7 space-y-4">
        <AuthAlert message={serverError} />
        <AuthAlert type="success" message={success} />
        <div className="grid gap-4 sm:grid-cols-2">
          <FormInput label="First name" icon={UserRound} autoComplete="given-name" placeholder="Ada" error={errors.firstName?.message} {...register('firstName')} />
          <FormInput label="Last name" autoComplete="family-name" placeholder="Okafor" error={errors.lastName?.message} {...register('lastName')} />
        </div>
        <FormInput label="Email" icon={Mail} type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')} />
        <FormInput label="Phone (optional)" icon={Phone} type="tel" autoComplete="tel" placeholder="+234 801 234 5678" error={errors.phone?.message} {...register('phone')} />
        <Controller name="roles" control={control} render={({ field }) => <RoleSelect value={field.value} onChange={field.onChange} error={errors.roles?.message} />} />
        <div>
          <PasswordInput label="Password" autoComplete="new-password" placeholder="Create a password" error={errors.password?.message} {...register('password')} />
          <PasswordStrength password={watch('password')} />
        </div>
        <PasswordInput label="Confirm password" autoComplete="new-password" placeholder="Repeat your password" error={errors.confirmPassword?.message} {...register('confirmPassword')} />

        <div>
          <label className="flex cursor-pointer items-start gap-2.5 text-sm text-slate-300">
            <input type="checkbox" aria-invalid={!!errors.terms} className="mt-0.5 h-4 w-4 rounded border-line bg-ink-800 accent-brand-500" {...register('terms')} />
            <span>
              I agree to the{' '}
              <Link to="/terms" className="font-semibold text-brand-400 underline underline-offset-2">Terms of Service</Link>{' '}and{' '}
              <Link to="/privacy" className="font-semibold text-accent underline underline-offset-2">Privacy Policy</Link>
            </span>
          </label>
          {errors.terms && <p role="alert" className="mt-1.5 text-xs font-medium text-red-300">Error: {errors.terms.message}</p>}
        </div>

        <LoadingButton type="submit" loading={isSubmitting} disabled={!!success}>
          {success ? 'Account created' : isSubmitting ? 'Creating account…' : 'Create Account'}
        </LoadingButton>
      </form>

      <p className="mt-7 text-center text-sm text-muted">
        Already have an account?{' '}
        <Link to="/login" className="font-bold text-brand-400 hover:underline">Sign in</Link>
      </p>
    </>
  )
}
