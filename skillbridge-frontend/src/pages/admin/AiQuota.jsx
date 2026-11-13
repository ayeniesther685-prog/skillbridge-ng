import { z } from 'zod'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import { adminApi } from '../../api/admin'
import { aiApi } from '../../api/ai'
import { useSave } from '../../hooks/useApi'
import { num } from '../../utils/validators'
export default function AiQuota() {
  const tiers = useQuery({ queryKey: ['ai', 'tiers'], queryFn: aiApi.tiers })
  const tl = (Array.isArray(tiers.data) ? tiers.data : tiers.data?.content || []).map((t) => t.name ?? t.tierName ?? t).filter((t) => typeof t === 'string')
  const grant = useSave(adminApi.grantQuota, { success: 'Credits granted' })
  const tier = useSave(adminApi.setTier, { success: 'Tier updated' })
  const uid = num('Enter a user ID').pipe(z.number().int().positive())
  return (
    <div className="grid max-w-4xl gap-6 md:grid-cols-2">
      <div className="md:col-span-2"><PageHeader title="AI quota" description="Grant extra requests or change a user's tier." /></div>
      <Card><CardBody><h3 className="mb-3 font-semibold">Grant credits</h3>
        <AutoForm schema={z.object({ userId: uid, amount: num('Enter an amount').pipe(z.number().int().positive('Must be more than 0')) })} defaultValues={{ userId: '', amount: '' }}
          fields={[{ name: 'userId', label: 'User ID', type: 'number', wide: true }, { name: 'amount', label: 'Credits', type: 'number', wide: true }]} onSubmit={(v) => grant.mutateAsync(v)} submitLabel="Grant" /></CardBody></Card>
      <Card><CardBody><h3 className="mb-3 font-semibold">Change tier</h3>
        <AutoForm schema={z.object({ userId: uid, tierName: z.string().min(1, 'Choose a tier') })} defaultValues={{ userId: '', tierName: '' }}
          fields={[{ name: 'userId', label: 'User ID', type: 'number', wide: true }, { name: 'tierName', label: 'Tier', type: 'select', options: tl, wide: true }]} onSubmit={(v) => tier.mutateAsync(v)} submitLabel="Update tier" /></CardBody></Card>
    </div>
  )
}
