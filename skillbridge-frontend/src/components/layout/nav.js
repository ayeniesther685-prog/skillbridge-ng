import { LayoutDashboard, Briefcase, GraduationCap, BadgeCheck, ShieldCheck, FileText, FileSignature, Wallet, Bot, MessageSquare, Bell, User, Trophy, Award, PlusCircle, Users, Settings, BookOpen } from 'lucide-react'
import { DollarSign } from 'lucide-react'

const I = (to, label, icon) => ({ to, label, icon })

export const navFor = (roles) => {
  const has = (r) => roles.includes(r)
  const out = []
  const add = (it) => !out.some((x) => x.to === it.to) && out.push(it)

  // Dashboard link for each held role
  ;['STUDENT', 'FREELANCER', 'CLIENT', 'ADMIN'].filter(has).forEach((r) =>
    add(I(
      `/dashboard/${r.toLowerCase()}`,
      `${r[0]}${r.slice(1).toLowerCase()} dashboard`,
      r === 'ADMIN' ? ShieldCheck : LayoutDashboard
    ))
  )

  if (has('STUDENT')) {
    add(I('/academy/courses', 'My courses', BookOpen))
    add(I('/academy/enroll', 'Enroll', GraduationCap))
    add(I('/academy/certificates', 'Certificates', Award))
  }

  if (has('STUDENT') || has('FREELANCER')) {
    add(I('/jobs', 'Find jobs', Briefcase))
    add(I('/applications', 'Applications', FileText))
  }

  if (has('CLIENT')) {
    add(I('/jobs/new', 'Post a job', PlusCircle))
    add(I('/jobs', 'Browse jobs', Briefcase))
    add(I('/freelancers', 'Freelancers', Users))
    // NEW: Provider verification entry for CLIENT
    add(I('/profile/provider-verification', 'Provider Verification', ShieldCheck))
  }

  if (has('STUDENT') || has('FREELANCER') || has('CLIENT')) {
    add(I('/contracts', 'Contracts', FileSignature))
  }

  if (has('STUDENT') || has('FREELANCER')) {
    add(I('/challenges', 'Challenges', Trophy))
    add(I('/wallet', 'Wallet', Wallet))
    add(I('/ai/tutor', 'AI tutor', Bot))
  }

  if (has('ADMIN')) {
    // NEW: Admin provider verification review entry
    add(I('/admin/provider-verifications', 'Provider Verifications', ShieldCheck))
    add(I('/admin/disputes', 'Disputes', ShieldCheck))
  }

  if (!has('ADMIN')) add(I('/profile', 'Profile', User))

  add(I('/courses', 'Course catalog', GraduationCap))
  add(I('/certificates/verify', 'Verify certificate', BadgeCheck))
  add(I('/settings', 'Settings', Settings))

  return out
}