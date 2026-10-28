import { Link } from 'react-router-dom'
import { Pencil } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import ProfileView from '../components/ProfileView.jsx'

export default function Profile() {
  const { user } = useAuth()
  return (
    <div>
      <div className="mb-4 flex justify-end">
        <Link to="/profile/edit" className="inline-flex h-11 items-center gap-2 rounded-xl bg-brand-500 px-4 text-sm font-bold hover:bg-brand-600"><Pencil className="h-4 w-4" />Edit profile</Link>
      </div>
      <ProfileView userId={user.id} basic={user} />
    </div>
  )
}
