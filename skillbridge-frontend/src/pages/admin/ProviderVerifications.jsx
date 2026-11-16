import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { providerVerificationApi } from '../../api/providerVerification';
import VerificationBadge from '../../components/ui/VerificationBadge';

const STATUS_OPTIONS = ['UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED', 'EXPIRED'];
const TYPE_OPTIONS = ['EMPLOYER', 'RECRUITMENT_AGENCY', 'TRAINING_PROVIDER'];

export default function ProviderVerifications() {
  const [status, setStatus] = useState('PENDING');
  const [type, setType] = useState('');
  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-provider-verifications', status, type, page],
    queryFn: () =>
      providerVerificationApi.adminList({
        status: status || undefined,
        type: type || undefined,
        page,
        size: 20,
      }),
  });

  return (
    <div className="space-y-4 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Provider Verifications</h1>
      </header>

      <div className="flex gap-2">
        <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} className="rounded border border-slate-700 bg-slate-900 px-3 py-2">
          <option value="">All statuses</option>
          {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        <select value={type} onChange={(e) => { setType(e.target.value); setPage(0); }} className="rounded border border-slate-700 bg-slate-900 px-3 py-2">
          <option value="">All types</option>
          {TYPE_OPTIONS.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
      </div>

      {isLoading && <div>Loading…</div>}
      {isError && <div className="text-rose-400">Failed to load.</div>}

      {data && (
        <div className="overflow-hidden rounded-lg border border-slate-800">
          <table className="w-full text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                <th className="p-3 text-left">Legal name</th>
                <th className="p-3 text-left">Type</th>
                <th className="p-3 text-left">Status</th>
                <th className="p-3 text-left">Submitted</th>
                <th className="p-3" />
              </tr>
            </thead>
            <tbody>
              {data.data.content.map(row => (
                <tr key={row.id} className="border-t border-slate-800 hover:bg-slate-900/50">
                  <td className="p-3">{row.legalName}</td>
                  <td className="p-3">{row.providerType}</td>
                  <td className="p-3"><VerificationBadge status={row.status} compact /></td>
                  <td className="p-3 text-slate-400">{row.submittedAt ? new Date(row.submittedAt).toLocaleString() : '—'}</td>
                  <td className="p-3 text-right">
                    <Link to={`/admin/provider-verifications/${row.id}`} className="text-indigo-400 hover:underline">Review</Link>
                  </td>
                </tr>
              ))}
              {data.data.content.length === 0 && (
                <tr><td colSpan={5} className="p-6 text-center text-slate-500">No verifications found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {data && data.data.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="rounded border border-slate-700 px-3 py-1 disabled:opacity-40">Previous</button>
          <span className="text-sm text-slate-400">Page {page + 1} of {data.data.totalPages}</span>
          <button disabled={page >= data.data.totalPages - 1} onClick={() => setPage(p => p + 1)} className="rounded border border-slate-700 px-3 py-1 disabled:opacity-40">Next</button>
        </div>
      )}
    </div>
  );
}