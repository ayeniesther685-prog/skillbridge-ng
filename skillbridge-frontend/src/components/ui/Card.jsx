export function Card({ className = '', hoverable, children, ...rest }) {
  return (
    <div
      className={`rounded-xl border bg-card transition-all ${
        hoverable
          ? 'cursor-pointer hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-lg hover:shadow-primary/5'
          : ''
      } ${className}`}
      {...rest}
    >
      {children}
    </div>
  )
}
export const CardHeader = ({ className = '', children }) => (
  <div className={`border-b px-5 py-4 ${className}`}>{children}</div>
)
export const CardBody = ({ className = '', children }) => (
  <div className={`p-5 ${className}`}>{children}</div>
)
export const CardFooter = ({ className = '', children }) => (
  <div className={`border-t px-5 py-3 ${className}`}>{children}</div>
)
export default Card