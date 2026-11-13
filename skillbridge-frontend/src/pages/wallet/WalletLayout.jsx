import { Outlet } from 'react-router-dom'
import { Info } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Tabs from '../../components/ui/Tabs'
export default function WalletLayout() {
  return (
    <div>
      <PageHeader title="Wallet" />
      <div className="mb-6 flex items-start gap-2 rounded-lg border border-warning/40 bg-warning/10 p-3 text-sm"><Info className="mt-0.5 h-4 w-4 shrink-0 text-warning" />
        <p>Withdrawals are recorded but not processed. Your balance is preserved until payment provider integration is live.</p></div>
      <Tabs tabs={[['/wallet', 'Overview'], ['/wallet/transactions', 'Transactions'], ['/wallet/withdraw', 'Withdraw'], ['/wallet/withdrawals', 'My requests']]} />
      <Outlet />
    </div>
  )
}
