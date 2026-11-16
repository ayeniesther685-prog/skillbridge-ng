import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { contractRevisionsApi } from '../../api/contractRevisions';
import { useAuth } from '../../context/AuthContext';

const STATUS_COLORS = {
  OPEN: 'text-amber-400 bg-amber-500/10 border-amber-500/30',
  ACCEPTED: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30',
  REJECTED: 'text-rose-400 bg-rose-500/10 border-rose-500/30',
  AUTO_ACCEPTED: 'text-sky-400 bg-sky-500/10 border-sky-500/30',
  ESCALATED_AS_NEW_REQUIREMENT: 'text-orange-400 bg-orange-500/10 border-orange-500/30',
};

export default function ContractRevisions() {
  const { contractId } = useParams();
  const { user } = useAuth();
  const qc = useQueryClient();

  const { data: revisions = [], isLoading, isError, error } = useQuery({
    queryKey: ['contract-revisions', contractId],
    queryFn: () => contractRevisionsApi.listForContract(contractId).then(r => r.data),
  });

  const [form, setForm] = useState({ reason: '', isNewRequirements: false, milestoneId: '' });

  const requestMutation = useMutation({
    mutationFn: ({ milestoneId, payload }) => contractRevisionsApi.requestRevision(milestoneId, payload),
    onSuccess: () => {
      setForm({ reason: '', isNewRequirements: false, milestoneId: '' });
      qc.invalidateQueries({ queryKey: ['contract-revisions', contractId] });
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.milestoneId) return;
    requestMutation.mutate({
      milestoneId: form.milestoneId,
      payload: { reason: form.reason, isNewRequirements: form.isNewRequirements },
    });
  };

  if (isLoading) return <div className="p-6 text-slate-400">Loading revisions…</div>;
  if (isError) return <div className="p-6 text-rose-400">Failed to load: {error?.response?.data?.message || error.message}</div>;

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Revisions</h1>
          <p className="text-sm text-slate-400">Contract #{contractId}</p>
        </div>
        <Link to={`/contracts/${contractId}`} className="text-sm text-indigo-400 hover:underline">
          ← Back to contract
        </Link>
      </header>

      {user?.roles?.includes('CLIENT') && (
        <section className="rounded-lg border border-slate-800 p-4">
          <h2 className="mb-3 text-sm font-medium text-slate-300">Request a revision</h2>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div>
              <label className="mb-1 block text-xs text-slate-400">Milestone ID</label>
              <input
                type="number"
                required
                value={form.milestoneId}
                onChange={(e) => setForm({ ...form, milestoneId: e.target.value })}
                className="w-full rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm"
                placeholder="e.g. 12"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs text-slate-400">Reason (what needs to change?)</label>
              <textarea
                required
                rows={3}
                value={form.reason}
                onChange={(e) => setForm({ ...form, reason: e.target.value })}
                className="w-full rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm"
                placeholder="Describe precisely what is wrong or missing from the submitted milestone."
              />
            </div>
            <label className="flex items-center gap-2 text-sm text-slate-300">
              <input
                type="checkbox"
                checked={form.isNewRequirements}
                onChange={(e) => setForm({ ...form, isNewRequirements: e.target.checked })}
              />
              This is new scope not covered by the original milestone (create a new milestone instead)
            </label>
            <button
              type="submit"
              disabled={requestMutation.isPending}
              className="rounded bg-indigo-600 px-4 py-2 text-sm font-medium hover:bg-indigo-500 disabled:opacity-50"
            >
              {requestMutation.isPending ? 'Submitting…' : 'Request revision'}
            </button>
            {requestMutation.isError && (
              <div className="rounded border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-300">
                {requestMutation.error?.response?.data?.message || 'Request failed.'}
              </div>
            )}
          </form>
        </section>
      )}

      <section className="space-y-3">
        <h2 className="text-sm font-medium text-slate-300">History ({revisions.length})</h2>
        {revisions.length === 0 && (
          <div className="rounded-lg border border-slate-800 p-6 text-center text-slate-500">
            No revisions yet.
          </div>
        )}
        {revisions.map((r) => (
          <div key={r.id} className="rounded-lg border border-slate-800 p-4">
            <div className="mb-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className={`rounded-full border px-2 py-0.5 text-xs ${STATUS_COLORS[r.status] || 'text-slate-400 border-slate-600'}`}>
                  {r.status}
                </span>
                <span className="text-xs text-slate-500">
                  milestone #{r.milestoneId} · {new Date(r.createdAt).toLocaleString()}
                </span>
                {r.isNewRequirements && (
                  <span className="rounded border border-orange-500/30 bg-orange-500/10 px-2 py-0.5 text-xs text-orange-300">
                    new scope
                  </span>
                )}
              </div>
              <Link to={`/contracts/${contractId}/revisions/${r.id}`} className="text-xs text-indigo-400 hover:underline">
                view →
              </Link>
            </div>
            <p className="text-sm text-slate-300">{r.reason}</p>
            {r.freelancerResponse && (
              <div className="mt-2 rounded border border-slate-800 bg-slate-900/50 p-2 text-sm text-slate-400">
                <span className="text-xs text-slate-500">Freelancer:</span> {r.freelancerResponse}
              </div>
            )}
          </div>
        ))}
      </section>
    </div>
  );
}
