import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { providerVerificationApi } from '../../api/providerVerification';
import VerificationBadge from '../../components/ui/VerificationBadge';
import Textarea from '../../components/ui/Textarea';
import Button from '../../components/ui/Button';

export default function ProviderVerificationDetail() {
  const { id } = useParams();
  const qc = useQueryClient();
  const nav = useNavigate();
  const [reason, setReason] = useState('');

  const { data: v, isLoading } = useQuery({
    queryKey: ['admin-provider-verification', id],
    queryFn: () => providerVerificationApi.adminGet(id).then(r => r.data),
  });
  const { data: events } = useQuery({
    queryKey: ['admin-provider-verification-events', id],
    queryFn: () => providerVerificationApi.adminEvents(id).then(r => r.data),
  });

  const action = useMutation({
    mutationFn: ({ action, reason }) => {
      const m = {
        approve: providerVerificationApi.adminApprove,
        reject: providerVerificationApi.adminReject,
        suspend: providerVerificationApi.adminSuspend,
        revoke: providerVerificationApi.adminRevoke,
      }[action];
      return m(id, reason);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-provider-verification', id] });
      qc.invalidateQueries({ queryKey: ['admin-provider-verification-events', id] });
      qc.invalidateQueries({ queryKey: ['admin-provider-verifications'] });
      setReason('');
    },
  });

  if (isLoading) return <div className="p-6">Loading…</div>;
  if (!v) return <div className="p-6">Not found.</div>;

  const canApprove = v.status === 'PENDING';
  const canReject = v.status === 'PENDING';
  const canSuspend = v.status === 'VERIFIED' || v.status === 'PENDING';
  const canRevoke = v.status !== 'UNVERIFIED';

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Review: {v.legalName}</h1>
        <button onClick={() => nav('/admin/provider-verifications')} className="text-slate-400 hover:underline">← Back</button>
      </div>

      <div className="rounded-lg border border-slate-800 p-4">
        <div className="mb-3"><VerificationBadge status={v.status} /></div>
        <dl className="grid grid-cols-1 gap-2 md:grid-cols-2">
          <div><dt className="text-slate-500">Type</dt><dd>{v.providerType}</dd></div>
          <div><dt className="text-slate-500">Legal name</dt><dd>{v.legalName}</dd></div>
          <div><dt className="text-slate-500">CAC</dt><dd>{v.cacRegistrationNumber || '—'}</dd></div>
          <div><dt className="text-slate-500">Official email</dt><dd>{v.officialEmail}</dd></div>
          <div><dt className="text-slate-500">Official phone</dt><dd>{v.officialPhone}</dd></div>
          <div className="md:col-span-2"><dt className="text-slate-500">Address</dt><dd>{v.physicalAddress}</dd></div>
          <div><dt className="text-slate-500">Website</dt><dd>{v.website || '—'}</dd></div>
          <div><dt className="text-slate-500">LinkedIn</dt><dd>{v.linkedinUrl || '—'}</dd></div>
          <div><dt className="text-slate-500">Authorized rep</dt><dd>{v.authorizedRepName} ({v.authorizedRepRole})</dd></div>
        </dl>
      </div>

      <div className="space-y-2">
        <Textarea label="Reason / notes (required)" value={reason} onChange={(e) => setReason(e.target.value)} rows={3} />
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => action.mutate({ action: 'approve', reason })} disabled={!canApprove || !reason || action.isPending}>Approve</Button>
          <Button onClick={() => action.mutate({ action: 'reject', reason })} disabled={!canReject || !reason || action.isPending}>Reject</Button>
          <Button onClick={() => action.mutate({ action: 'suspend', reason })} disabled={!canSuspend || !reason || action.isPending}>Suspend</Button>
          <Button onClick={() => action.mutate({ action: 'revoke', reason })} disabled={!canRevoke || !reason || action.isPending}>Revoke</Button>
        </div>
        {action.isError && <div className="text-sm text-rose-400">{action.error?.response?.data?.message || 'Action failed.'}</div>}
      </div>

      <div>
        <h2 className="mb-2 text-lg font-semibold">Event history</h2>
        <div className="space-y-2">
          {events?.map(e => (
            <div key={e.id} className="rounded border border-slate-800 p-3 text-sm">
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs text-slate-400">{e.eventType}</span>
                <span className="text-xs text-slate-500">{new Date(e.createdAt).toLocaleString()}</span>
              </div>
              <div className="text-slate-300">
                {e.fromStatus || '—'} → {e.toStatus || '—'}
              </div>
              {e.notes && <div className="mt-1 text-slate-400">{e.notes}</div>}
            </div>
          ))}
          {(!events || events.length === 0) && <div className="text-slate-500">No events yet.</div>}
        </div>
      </div>
    </div>
  );
}