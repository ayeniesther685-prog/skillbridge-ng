import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../../components/ui/PageHeader'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import ComingSoon from '../../components/ui/ComingSoon'
export default function Freelancers() {
  const [id, setId] = useState(''); const nav = useNavigate()
  return (
    <div className="max-w-xl space-y-6">
      <PageHeader title="Freelancer profiles" description="Open a public profile by user ID." />
      <form className="flex items-end gap-2" onSubmit={(e) => { e.preventDefault(); id && nav(`/freelancers/${id}`) }}>
        <Input className="flex-1" label="User ID" type="number" min="1" value={id} onChange={(e) => setId(e.target.value)} /><Button type="submit" disabled={!id}>Open profile</Button>
      </form>
      <ComingSoon title="Browse and search freelancers" description="List and filter freelancers by skill and rate." />
    </div>
  )
}
