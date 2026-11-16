import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { contractRevisionsApi } from '../../api/contractRevisions';

export default function RevisionResponse() {
  const { contractId, revisionId } = useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [response, setResponse] = useState('');
  const [accept, setAccept] = useState(true);

  const { data: rev, isLoading, isError, error } = useQuery({
    queryKey: ['contract-revision', revisionId],
    queryFn: () => contractRevisionsApi.getOne(revisionId).then(r => r.data),
  });

  useEffect(() => {
    if (rev?.freelancerResponse) {
      setResponse(rev.freelancerResponse);
      setAccept(rev.status === 'ACCEPTED');
    }
  }, [rev]);

  const mutation = useMutation({
    mutationFn: (payload) => contractRevisionsApi.respond(revisionId, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['contract-revision', revisionId] });
      qc.invalidateQueries({ queryKey: ['contract-revisions', contractId] });
      navigate(`/contracts/${contractId}/revisions`);
    },
  });

  if (isLoading) return <div className="p-6 text-slate-400">Loading…</div>;
  if (isError) return <div className="p-6 text-rose-400">{error?.response?.data?.message || 'Failed to load.'}</div>;
  if (!rev) return <div className="p-6">Not found.</div>;

  const isOpen = rev.status === 'OPEN';

  return (
    <div className="mx-auto max-w-3xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Revision request</h1>
          <p className="text-sm text-slate-400">
            Contract #{contractId} · Milestone #{rev.milestoneId}
          </p>
        </div>
        <Link to={`/contracts/${contractId}/revisions`} className="text-sm text-indigo-400 hover:underline">
          ← Back
        </Link>
      </header>

      <div className="rounded-lg border border-slate-800 p-4">
        <div className="mb-2 flex items-center gap-2">
          <span className="rounded-full border border-slate-600 px-2 py-0.5 text-xs text-slate-300">
            {rev.status}
          </span>
          {rev.isNewRequirements && (
            <span className="rounded border border-orange-500/30 bg-orange-500/10 px-2 py-0.5 text-xs text-orange-300">
              requested as new scope
            </span>
          )}
          <span className="text-xs text-slate-500">
            requested by {rev.requestedByName || 'client'} · {new Date(rev.createdAt).toLocaleString()}
          </span>
        </div>
        <p className="text-sm text-slate-200">{rev.reason}</p>
      </div>

      {isOpen ? (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            mutation.mutate({ accept, response });
          }}
          className="space-y-3 rounded-lg border border-slate-800 p-4"
        >
          <h2 className="text-sm font-medium text-slate-300">Respond</h2>
          <div className="flex gap-4">
            <label className="flex items-center gap-2 text-sm">
              <input type="radio" checked={accept} onChange={() => setAccept(true)} />
              Accept revision
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input type="radio" checked={!accept} onChange={() => setAccept(false)} />
              Reject
            </label>
          </div>
          <textarea
            required
            rows={4}
            value={response}
            onChange={(e) => setResponse(e.target.value)}
            placeholder={accept ? "Describe what you'll change and when." : "Explain why this is not within the original milestone."}
            className="w-full rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm"
          />
          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded bg-indigo-600 px-4 py-2 text-sm font-medium hover:bg-indigo-500 disabled:opacity-50"
          >
            {mutation.isPending ? 'Submitting…' : accept ? 'Accept revision' : 'Reject revision'}
          </button>
          {mutation.isError && (
            <div className="rounded border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-300">
              {mutation.error?.response?.data?.message || 'Failed.'}
            </div>
          )}
        </form>
      ) : (
        <div className="rounded-lg border border-slate-800 p-4">
          <h2 className="mb-2 text-sm font-medium text-slate-300">Your response</h2>
          <p className="text-sm text-slate-300">{rev.freelancerResponse || '—'}</p>
          {rev.resolvedAt && (
            <p className="mt-2 text-xs text-slate-500">Resolved {new Date(rev.resolvedAt).toLocaleString()}</p>
          )}
        </div>
      )}
    </div>
  );
}
