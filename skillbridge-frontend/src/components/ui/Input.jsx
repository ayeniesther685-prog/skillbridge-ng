import { forwardRef, useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

const Input = forwardRef(function Input(
  { label, error, hint, className = '', id, type = 'text', ...rest },
  ref
) {
  const key = id || rest.name
  const [revealed, setRevealed] = useState(false)
  const isPassword = type === 'password'
  const actualType = isPassword && revealed ? 'text' : type

  return (
    <div className={className}>
      {label && (
        <label htmlFor={key} className="mb-1.5 block text-sm font-medium">
          {label}
        </label>
      )}

      <div className={isPassword ? 'relative' : ''}>
        <input
          id={key}
          ref={ref}
          type={actualType}
          aria-invalid={!!error}
          className={`field ${error ? 'border-danger' : ''} ${isPassword ? 'pr-10' : ''}`}
          {...rest}
        />

        {isPassword && (
          <button
            type="button"
            onClick={() => setRevealed((v) => !v)}
            aria-label={revealed ? 'Hide password' : 'Show password'}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-foreground focus:outline-none"
            tabIndex={-1}
          >
            {revealed ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>

      {error ? (
        <p className="mt-1 text-xs text-danger">{error}</p>
      ) : hint ? (
        <p className="mt-1 text-xs text-muted">{hint}</p>
      ) : null}
    </div>
  )
})

export default Input