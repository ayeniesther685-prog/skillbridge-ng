import { Link } from 'react-router-dom'
import { GraduationCap, Briefcase, Bot, Wallet } from 'lucide-react'
import Button from '../../components/ui/Button'
import Card from '../../components/ui/Card'
const features = [
  [GraduationCap, 'Learn in the Academy', 'Programs, departments and courses with lessons, assignments and verifiable certificates.'],
  [Briefcase, 'Find freelance work', 'Browse open jobs, apply, sign contracts and track milestones with clients.'],
  [Bot, 'Study with an AI tutor', 'Ask questions any time, within a daily quota you can see.'],
  [Wallet, 'Track your earnings', 'See wallet balance and transactions, and record withdrawal requests.'],
]
export default function Landing() {
  return (
    <div>
      <section className="py-16 sm:py-24">
        <h1 className="max-w-3xl text-4xl font-semibold leading-tight tracking-tight sm:text-5xl">Connect skills to opportunity.</h1>
        <p className="mt-4 max-w-xl text-lg text-muted">SkillBridge NG brings learning, freelance work and career growth into one place for Nigerian students, freelancers and clients.</p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Button as={Link} to="/register" size="lg">Create a free account</Button>
          <Button as={Link} to="/jobs" variant="outline" size="lg">Browse open jobs</Button>
        </div>
      </section>
      <section className="grid gap-4 sm:grid-cols-2">
        {features.map(([Icon, t, d]) => (
          <Card key={t} className="p-5"><Icon className="mb-3 h-5 w-5 text-accent" /><h3 className="font-semibold">{t}</h3><p className="mt-1 text-sm text-muted">{d}</p></Card>
        ))}
      </section>
      <section className="mt-16 rounded-lg border bg-card p-6">
        <h2 className="text-xl font-semibold">How it works</h2>
        <ol className="mt-4 grid gap-4 text-sm sm:grid-cols-3">
          <li><b>1. Sign up</b><p className="mt-1 text-muted">Choose Student, Freelancer, Client — or more than one.</p></li>
          <li><b>2. Learn or apply</b><p className="mt-1 text-muted">Enrol in courses, or apply to jobs that match your skills.</p></li>
          <li><b>3. Deliver and grow</b><p className="mt-1 text-muted">Complete lessons and milestones, earn certificates and reviews.</p></li>
        </ol>
      </section>
    </div>
  )
}
