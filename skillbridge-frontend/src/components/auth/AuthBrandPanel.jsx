import { motion, useReducedMotion } from 'framer-motion'
import { BadgeCheck, Briefcase, GraduationCap, Handshake, Search, Sparkles, UserRound, Wallet } from 'lucide-react'
import Logo from '../common/Logo.jsx'

function Float({ children, className, delay = 0 }) {
  const reduce = useReducedMotion()
  return (
    <motion.div className={`absolute rounded-2xl border border-line bg-ink-800/80 p-3.5 shadow-xl shadow-black/30 backdrop-blur ${className}`}
      animate={reduce ? undefined : { y: [0, -8, 0] }} transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut', delay }}>
      {children}
    </motion.div>
  )
}
const Row = ({ icon: Icon, tone, top, bottom }) => (
  <div className="flex items-center gap-3">
    <span className={`grid h-9 w-9 place-items-center rounded-lg ${tone}`}><Icon className="h-4 w-4" /></span>
    <div>{top && <p className="text-xs text-muted">{top}</p>}<p className="text-sm font-bold">{bottom}</p></div>
  </div>
)
const roles = [
  { label: 'Freelancer', icon: Sparkles, pos: 'left-0 top-2' },
  { label: 'Student', icon: GraduationCap, pos: 'right-2 top-16' },
  { label: 'Client', icon: Handshake, pos: 'left-12 top-[8.5rem]' },
  { label: 'Recruiter', icon: Search, pos: 'right-0 top-[13rem]' },
  { label: 'Mentor', icon: UserRound, pos: 'left-4 top-[17.5rem]' },
]

export default function AuthBrandPanel({ variant = 'login' }) {
  const isLogin = variant === 'login'
  return (
    <aside className="relative hidden overflow-hidden border-r border-line bg-ink-900 p-12 lg:flex lg:flex-col">
      <div className="grid-bg absolute inset-0" aria-hidden="true" />
      <div className="absolute -left-24 -top-24 h-72 w-72 rounded-full bg-brand-500/25 blur-3xl" aria-hidden="true" />
      <div className="absolute -bottom-24 right-0 h-64 w-64 rounded-full bg-accent/10 blur-3xl" aria-hidden="true" />
      <div className="relative z-10"><Logo /></div>
      <div className="relative z-10 mt-14 max-w-md">
        <h1 className="text-4xl font-extrabold leading-tight tracking-tight xl:text-5xl">{isLogin ? 'Connect skills to opportunity.' : 'Build your future on SkillBridge.'}</h1>
        <p className="mt-5 text-base leading-relaxed text-muted">
          {isLogin ? 'Discover opportunities, showcase your skills and connect with people who need what you can do.'
            : 'Create your profile, showcase your skills, discover opportunities and connect with people who need what you can do.'}
        </p>
      </div>
      <div className="relative z-10 mt-auto h-[24rem] max-w-md" aria-hidden="true">
        {isLogin ? (
          <>
            <Float className="left-0 top-4 w-60"><Row icon={Briefcase} tone="bg-brand-500/20 text-brand-400" top="New Job Posted" bottom="Frontend Developer" /></Float>
            <Float className="right-0 top-28 w-56" delay={1.2}><Row icon={Wallet} tone="bg-accent/20 text-accent" top="Fixed price" bottom="₦150,000 Project" /></Float>
            <Float className="left-10 top-60 w-56" delay={2.4}><Row icon={BadgeCheck} tone="bg-emerald-500/20 text-emerald-400" bottom="Profile Verified" /></Float>
          </>
        ) : roles.map((r, i) => (
          <Float key={r.label} className={`${r.pos} w-44`} delay={i * 0.8}><Row icon={r.icon} tone="bg-brand-500/20 text-brand-400" bottom={r.label} /></Float>
        ))}
      </div>
    </aside>
  )
}
