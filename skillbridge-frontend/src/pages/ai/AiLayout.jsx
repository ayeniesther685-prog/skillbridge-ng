import { Outlet } from 'react-router-dom'
import PageHeader from '../../components/ui/PageHeader'
import Tabs from '../../components/ui/Tabs'
export default function AiLayout() {
  return <div><PageHeader title="AI" description="Your tutor and daily usage." /><Tabs tabs={[['/ai/tutor', 'Tutor'], ['/ai/quota', 'Quota & usage']]} /><Outlet /></div>
}
