import { Outlet } from 'react-router-dom'
import PageHeader from '../../components/ui/PageHeader'
import Tabs from '../../components/ui/Tabs'
export default function ProfileLayout() {
  return (
    <div>
      <PageHeader title="Profile" description="What clients and the Academy see about you." />
      <Tabs tabs={[['/profile', 'Overview'], ['/profile/edit', 'Details'], ['/profile/skills', 'Skills'], ['/profile/education', 'Education'], ['/profile/experience', 'Experience'], ['/profile/certifications', 'Certifications'], ['/profile/portfolio', 'Portfolio']]} />
      <Outlet />
    </div>
  )
}
