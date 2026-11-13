import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { walletApi } from '../../api/wallet'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import QueryBoundary from '../../components/ui/QueryBoundary'
import ComingSoon from '../../components/ui/ComingSoon'
import TxTable from './TxTable'
import { naira } from '../../utils/format'
export const points = (w) => w.balancePoints ?? w.points ?? w.balance ?? 0
export default function WalletHome() {
  const w = useQuery({ queryKey: ['wallet'], queryFn: walletApi.me })
  const tx = useQuery({ queryKey: ['wallet', 'tx', 'recent'], queryFn: () => walletApi.transactions({ page: 0, size: 5 }) })
  return (
    <div className="space-y-6">
      <QueryBoundary q={w} rows={1}>{(d) => (
        <Card><CardBody className="flex flex-wrap items-end justify-between gap-4">
          <div><p className="text-sm text-muted">Balance</p><p className="text-3xl font-semibold">{points(d).toLocaleString()} <span className="text-base font-normal text-muted">points</span></p>
            {(d.balanceNaira ?? d.nairaValue) != null && <p className="text-sm text-muted">≈ {naira(d.balanceNaira ?? d.nairaValue)}</p>}</div>
          <Button as={Link} to="/wallet/withdraw">Request withdrawal</Button>
        </CardBody></Card>)}</QueryBoundary>
      <div><div className="mb-3 flex items-center justify-between"><h2 className="font-semibold">Recent transactions</h2><Link to="/wallet/transactions" className="text-sm text-accent hover:underline">View all</Link></div>
        <QueryBoundary q={tx} rows={3}>{(d) => <TxTable rows={d.content} />}</QueryBoundary></div>
      <ComingSoon title="Bank payouts" description="Receive withdrawals directly in your bank account." />
    </div>
  )
}
