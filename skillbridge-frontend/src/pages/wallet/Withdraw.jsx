import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useQuery } from '@tanstack/react-query'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import { walletApi } from '../../api/wallet'
import { useSave } from '../../hooks/useApi'
import { num, reqStr } from '../../utils/validators'
import { points } from './WalletHome'
export default function Withdraw() {
  const nav = useNavigate()
  const w = useQuery({ queryKey: ['wallet'], queryFn: walletApi.me })
  const withdraw = useSave(walletApi.withdraw, { invalidate: [['wallet']], success: 'Withdrawal request recorded', onSuccess: () => nav('/wallet/withdrawals') })
  const bal = w.data ? points(w.data) : null
  const schema = z.object({
    amountPoints: num('Enter an amount').pipe(z.number().int('Use whole points').positive('Must be more than 0')).refine((v) => bal == null || v <= bal, 'More than your balance'),
    bankName: reqStr('Enter the bank name', 100), accountNumber: z.string().trim().regex(/^\d{10}$/, 'Account numbers have 10 digits'), accountName: reqStr('Enter the account name', 150),
  })
  return (
    <Card className="max-w-xl"><CardBody>
      <p className="mb-4 text-sm text-muted">{bal != null ? `Available: ${bal.toLocaleString()} points.` : ''} This records a request; no money is transferred yet.</p>
      <AutoForm key={bal ?? 'x'} schema={schema} defaultValues={{ amountPoints: '', bankName: '', accountNumber: '', accountName: '' }}
        fields={[{ name: 'amountPoints', label: 'Amount (points)', type: 'number' }, { name: 'bankName', label: 'Bank name' }, { name: 'accountNumber', label: 'Account number' }, { name: 'accountName', label: 'Account name' }]}
        onSubmit={(v) => withdraw.mutateAsync(v)} submitLabel="Record request" />
    </CardBody></Card>
  )
}
