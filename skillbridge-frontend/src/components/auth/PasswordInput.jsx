import { forwardRef, useState } from 'react'
import { Eye, EyeOff, Lock } from 'lucide-react'
import FormInput from './FormInput.jsx'

const PasswordInput = forwardRef(function PasswordInput(props, ref) {
  const [show, setShow] = useState(false)
  return (
    <FormInput ref={ref} icon={Lock} type={show ? 'text' : 'password'}
      rightSlot={
        <button type="button" onClick={() => setShow((s) => !s)} aria-label={show ? 'Hide password' : 'Show password'} aria-pressed={show}
          className="grid h-9 w-9 place-items-center rounded-lg text-muted transition-colors hover:text-white">
          {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
        </button>
      }
      {...props} />
  )
})
export default PasswordInput
