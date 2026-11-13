import { NavLink } from 'react-router-dom'
export default function Tabs({ tabs }) {
  return (
    <div className="mb-6 flex gap-1 overflow-x-auto border-b">
      {tabs.map(([to, label]) => (
        <NavLink key={to} to={to} end className={({ isActive }) => `whitespace-nowrap border-b-2 px-3 py-2.5 text-sm ${isActive ? 'border-primary text-fg' : 'border-transparent text-muted hover:text-fg'}`}>{label}</NavLink>
      ))}
    </div>
  )
}
