import { Link, useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { ProfileBody } from '../profile/ProfileView'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import { reviewsApi } from '../../api/reviews'
import { messagesApi } from '../../api/messages'
import { useAuth } from '../../context/AuthContext'
import { useSave } from '../../hooks/useApi'
export default function FreelancerProfile() {
  const { userId } = useParams(); const { user } = useAuth(); const nav = useNavigate()
  const rating = useQuery({ queryKey: ['rating', userId], queryFn: () => reviewsApi.rating(userId), enabled: !!user })
  const start = useSave(messagesApi.start, { onSuccess: (c) => nav(`/messages/${c.id}`) })
  return (
    <div className="max-w-3xl">
      <Breadcrumbs items={[['Freelancers', '/freelancers'], [`User #${userId}`]]} />
      <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
        <div>{rating.data?.average != null && <Badge variant="warning"><Star className="mr-1 h-3 w-3" />{Number(rating.data.average).toFixed(1)} average rating</Badge>}</div>
        {user ? (String(user.id) !== userId && <Button size="sm" loading={start.isPending} onClick={() => start.mutate(Number(userId))}>Send message</Button>) : <Button as={Link} to={`/login?next=/freelancers/${userId}`} size="sm">Log in to message</Button>}
      </div>
      <ProfileBody userId={userId} />
    </div>
  )
}
