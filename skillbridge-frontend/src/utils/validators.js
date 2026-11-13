import { z } from 'zod'
export const loginSchema = z.object({
  email: z.string().min(1, 'Enter your email').email('Enter a valid email'),
  password: z.string().min(1, 'Enter your password'),
})
export const registerSchema = z.object({
  firstName: z.string().trim().min(1, 'Enter your first name').max(100),
  lastName: z.string().trim().min(1, 'Enter your last name').max(100),
  email: z.string().min(1, 'Enter your email').email('Enter a valid email'),
  phone: z.string().optional(),
  password: z.string().min(8, 'Use at least 8 characters').max(100),
  roles: z.array(z.enum(['STUDENT', 'FREELANCER', 'CLIENT'])).min(1, 'Pick at least one role'),
})

// Number input helper: '' -> undefined, otherwise Number
export const num = (msg = 'Enter a number') =>
  z.preprocess((v) => (v === '' || v == null || Number.isNaN(v) ? undefined : Number(v)), z.number({ required_error: msg, invalid_type_error: msg }))
export const optNum = () => num().optional()
export const optStr = () => z.string().optional()
export const reqStr = (msg = 'Required', max = 5000) => z.string().trim().min(1, msg).max(max)
export const optUrl = () => z.string().trim().refine((v) => !v || /^https?:\/\/\S+$/i.test(v), 'Enter a full link starting with http:// or https://').optional()
