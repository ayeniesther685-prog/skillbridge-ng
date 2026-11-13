import { walletApi } from '../../api/wallet'
import { usePaged } from '../../hooks/useApi'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Pagination from '../../components/ui/Pagination'
import TxTable from './TxTable'
export default function Transactions() {
  const { q, setPage } = usePaged(['wallet', 'tx'], walletApi.transactions)
  return <QueryBoundary q={q}>{(d) => <><TxTable rows={d.content} /><Pagination page={d} onPage={setPage} /></>}</QueryBoundary>
}
