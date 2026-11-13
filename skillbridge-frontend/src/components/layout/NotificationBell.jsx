import { Bell } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { notificationsApi } from '../../api/notifications'
export default function NotificationBell() {
  const { data } = useQuery({ queryKey: ['notifications', 'unread'], queryFn: notificationsApi.unreadCount, refetchInterval: 60000 })
  const n = data?.count ?? 0
  return (
    <Link to="/notifications" className="relative rounded p-2 text-muted hover:bg-fg/5 hover:text-fg" aria-label={`Notifications, ${n} unread`}>
      <Bell className="h-4 w-4" />
      {n > 0 && <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-warning px-1 text-[10px] font-semibold text-black">{n > 99 ? '99+' : n}</span>}
    </Link>
  )
}
