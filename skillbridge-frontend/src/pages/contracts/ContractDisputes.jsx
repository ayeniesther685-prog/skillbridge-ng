import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { disputesApi } from '../../api/disputes';

const STATUS_COLOR = {
  OPEN: 'text-amber-400',
  UNDER_REVIEW: 'text-sky-400',
  RESOLVED_FOR_CLIENT: 'text-emerald-400',
  RESOLVED_FOR_FREELANCER: 'text-emerald-400',
  WITHDRAWN: 'text-slate-400',
  ESCALATED: 'text-rose-400',
};

export default function ContractDisputes() {
  const { contractId } = useParams();

  const { data: disputes = [], isLoading, isError, error } = useQuery({
    queryKey: ['contract-disputes', contractId],
    queryFn: () => disputesApi.listForContract(contractId),
  });

  if (isLoading) return <div className="p-6 text-slate-400">Loading…</div>;
  if (isError) return <div className="p-6 text-rose-400">{error?.response?.data?.message || 'Failed to load'}</div>;

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Disputes</h1>
          <p className="text-sm text-slate-400">Contract #{contractId}</p>
        </div>
        <Link to={`/contracts/${contractId}`} className="text-sm text-indigo-400 hover:underline">← Back</Link>
      </header>

      {disputes.length === 0 ? (
        <div className="rounded-lg border border-slate-800 p-6 text-center text-slate-500">
          No disputes on this contract.
        </div>
      ) : (
        <div className="space-y-2">
          {disputes.map((d) => (
            <Link key={d.id} to={`/contracts/${contractId}/disputes/${d.id}`} className="block">
              <div className="rounded-lg border border-slate-800 p-4 hover:border-slate-700">
                <div className="mb-1 flex items-center justify-between">
                  <span className={`text-xs font-medium ${STATUS_COLOR[d.status] || 'text-slate-400'}`}>
                    {d.status.replace(/_/g, ' ')}
                  </span>
                  <span className="text-xs text-slate-500">{new Date(d.createdAt).toLocaleString()}</span>
                </div>
                <p className="text-sm text-slate-300 line-clamp-2">{d.reason}</p>
                <p className="mt-1 text-xs text-slate-500">
                  Opened by {d.openedByName}
                  {d.milestoneId && ` · Milestone #${d.milestoneId}`}
                </p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
