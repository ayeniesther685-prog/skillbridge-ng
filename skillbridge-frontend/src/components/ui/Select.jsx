import { forwardRef } from 'react'
const Select = forwardRef(function Select({ label, className = '', id, children, ...rest }, ref) {
  const key = id || rest.name
  return (
    <div className={className}>
      {label && <label htmlFor={key} className="mb-1.5 block text-sm font-medium">{label}</label>}
      <select id={key} ref={ref} className="field" {...rest}>{children}</select>
    </div>
  )
})
export default Select
