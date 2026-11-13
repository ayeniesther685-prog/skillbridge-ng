import { Loader2 } from 'lucide-react'
const variants = {
  default: 'bg-primary text-white hover:bg-primary/90',
  outline: 'border bg-transparent hover:bg-fg/5',
  ghost: 'bg-transparent hover:bg-fg/5',
  destructive: 'bg-danger text-white hover:bg-danger/90',
}
const sizes = { sm: 'h-8 px-3 text-sm', md: 'h-10 px-4 text-sm', lg: 'h-12 px-6 text-base' }
export default function Button({ variant = 'default', size = 'md', loading, disabled, className = '', children, as: Tag = 'button', ...rest }) {
  return (
    <Tag disabled={disabled || loading} aria-busy={loading || undefined}
      className={`inline-flex items-center justify-center gap-2 rounded font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${sizes[size]} ${className}`} {...rest}>
      {loading && <Loader2 className="h-4 w-4 animate-spin" />}
      {children}
    </Tag>
  )
}
