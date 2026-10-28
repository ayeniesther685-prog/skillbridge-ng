import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="grid place-items-center py-24 text-center">
      <p className="text-6xl font-extrabold text-brand-400">404</p>
      <h1 className="mt-3 text-2xl font-extrabold">Page not found</h1>
      <p className="mt-1 text-muted">The page you're looking for doesn't exist.</p>
      <Link to="/" className="mt-6 rounded-xl bg-brand-500 px-5 py-2.5 text-sm font-bold hover:bg-brand-600">Back to home</Link>
    </div>
  )
}
