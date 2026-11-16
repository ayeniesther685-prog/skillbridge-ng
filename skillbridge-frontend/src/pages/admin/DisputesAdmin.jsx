import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { disputesApi } from '../../api/disputes';
import { useToast } from '../../context/ToastContext';

const STATUSES = ['OPEN', 'UNDER_REVIEW', 'RESOLVED_FOR_CLIENT', 'RESOLVED_FOR_FREELANCER', 'WITHDRAWN', 'ESCALATED'];
const STATUS_COLOR = {
  OPEN: 'text-amber-400',
  UNDER_REVIEW: 'text-sky-400',
  RESOLVED_FOR_CLIENT: 'text-emerald-400',
  RESOLVED_FOR_FREELANCER: 'text-emerald-400',
  WITHDRAWN: 'text-slate-400',
  ESCALATED: 'text-rose-400',
};
const TERMINAL = ['RESOLVED_FOR_CLIENT', 'RESOLVED_FOR_FREELANCER', 'WITHDRAWN'];

export default function DisputesAdmin() {
  const qc = useQueryClient();
  const toast = useToast();
  const [status, setStatus] = useState('OPEN');
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['admin-disputes', status, page],
    queryFn: () => disputesApi.adminList({ status: status || undefined, page, size: 20 }),
  });

  const list = data?.content || [];

  const [resolveState, setResolveState] = useState({ id: null, decision: 'RESOLVED_FOR_CLIENT', resolution: '' });

  const resolve = useMutation({
    mutationFn: () => disputesApi.adminResolve(resolveState.id, {
      decision: resolveState.decision,
      resolution: resolveState.resolution,
    }),
    onSuccess: () => {
      toast.success('Dispute resolved');
      setResolveState({ id: null, decision: 'RESOLVED_FOR_CLIENT', resolution: '' });
      qc.invalidateQueries({ queryKey: ['admin-disputes'] });
    },
    onError: (e) => toast.error(e?.response?.data?.message || 'Failed'),
  });

  return (
    <div className="space-y-4 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Disputes</h1>
      </header>

      <div className="flex gap-2">
        <select
          value={status}
          onChange={(e) => { setStatus(e.target.value); setPage(0); }}
          className="rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm"
        >
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {isLoading && <div>Loading…</div>}
      {list.length === 0 && !isLoading && (
        <div className="rounded-lg border border-slate-800 p-6 text-center text-slate-500">
          No disputes match this filter.
        </div>
      )}

      {list.length > 0 && (
        <div className="space-y-2">
          {list.map((d) => {
            const isTerminal = TERMINAL.includes(d.status);
            const isResolving = resolveState.id === d.id;
            return (
              <div key={d.id} className="rounded-lg border border-slate-800 p-4">
                <div className="mb-1 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className={`text-xs font-medium ${STATUS_COLOR[d.status] || 'text-slate-400'}`}>
                      {d.status.replace(/_/g, ' ')}
                    </span>
                    <span className="text-xs text-slate-500">#{d.id}</span>
                  </div>
                  <span className="text-xs text-slate-500">{new Date(d.createdAt).toLocaleString()}</span>
                </div>
                <p className="text-sm text-slate-300">{d.reason}</p>
                <p className="mt-1 text-xs text-slate-500">
                  Contract #{d.contractId} · Opened by {d.openedByName}
                  {d.milestoneId && ` · Milestone #${d.milestoneId}`}
                </p>

                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <Link to={`/contracts/${d.contractId}/disputes/${d.id}`} className="text-xs text-indigo-400 hover:underline">
                    View thread
                  </Link>
                  {!isTerminal && !isResolving && (
                    <button
                      onClick={() => setResolveState({ id: d.id, decision: 'RESOLVED_FOR_CLIENT', resolution: '' })}
                      className="text-xs text-emerald-400 hover:underline"
                    >
                      Resolve
                    </button>
                  )}
                </div>

                {isResolving && (
                  <div className="mt-3 space-y-2 rounded border border-slate-700 bg-slate-900/50 p-3">
                    <select
                      value={resolveState.decision}
                      onChange={(e) => setResolveState({ ...resolveState, decision: e.target.value })}
                      className="w-full rounded border border-slate-700 bg-slate-900 px-2 py-1 text-sm"
                    >
                      <option value="RESOLVED_FOR_CLIENT">Resolved for client</option>
                      <option value="RESOLVED_FOR_FREELANCER">Resolved for freelancer</option>
                      <option value="WITHDRAWN">Withdrawn</option>
                      <option value="ESCALATED">Escalate</option>
                    </select>
                    <textarea
                      rows={3}
                      placeholder="Resolution notes (min 20 chars)"
                      value={resolveState.resolution}
                      onChange={(e) => setResolveState({ ...resolveState, resolution: e.target.value })}
                      className="w-full rounded border border-slate-700 bg-slate-900 px-2 py-1 text-sm"
                    />
                    <div className="flex gap-2">
                      <button
                        disabled={resolveState.resolution.length < 20 || resolve.isPending}
                        onClick={() => resolve.mutate()}
                        className="rounded bg-emerald-600 px-3 py-1 text-sm disabled:opacity-50"
                      >
                        {resolve.isPending ? 'Resolving…' : 'Record resolution'}
                      </button>
                      <button
                        onClick={() => setResolveState({ id: null, decision: 'RESOLVED_FOR_CLIENT', resolution: '' })}
                        className="rounded border border-slate-700 px-3 py-1 text-sm"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
