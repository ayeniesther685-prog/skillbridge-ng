import PageHeader from '../../components/ui/PageHeader'
import Card, { CardBody } from '../../components/ui/Card'
const pages = {
  about: ['About SkillBridge NG', ['SkillBridge NG helps Nigerians build skills and turn them into income. It combines an online Academy, a freelance marketplace and career tools in one account.', 'Students learn through programs, courses and assignments and earn certificates anyone can verify. Freelancers and clients find each other through jobs, applications, contracts and milestones.']],
  faq: ['Frequently asked questions', ['Can I have more than one role? Yes. Pick any mix of Student, Freelancer and Client when you register.', 'Can I register as an admin? No. Admin accounts are created by the platform team.', 'Are payments and bank payouts available? Not yet. Withdrawal requests are recorded, but money is not transferred in this version.', 'Can I upload files? Not yet. Where a file is needed, paste a link to it.', 'Can I reset my password? Password reset is planned for v2.']],
  pricing: ['Pricing', ['SkillBridge NG has no paid plans or billing in this version. Subscriptions are planned for v2 and will be announced here.']],
  contact: ['Contact', ['A contact form needs a backend endpoint that is not available yet. Until then, reach the team through the channel your organisation gave you.']],
  terms: ['Terms of use', ['Use SkillBridge NG honestly: provide accurate information, respect other users, and do not misuse the platform or its AI tutor.', 'Contracts and milestones record agreements between clients and freelancers. SkillBridge NG does not currently process payments between them.']],
  privacy: ['Privacy', ['We store the account and profile details you provide so the platform can work: your name, email, roles, activity and messages.', 'Public profile information is visible to other visitors. You can edit your own profile details at any time.']],
}
export default function StaticPage({ page }) {
  const [title, paras] = pages[page]
  return (
    <div className="max-w-2xl"><PageHeader title={title} />
      <Card><CardBody className="space-y-4 text-sm leading-relaxed text-muted">{paras.map((p) => <p key={p}>{p}</p>)}</CardBody></Card></div>
  )
}
