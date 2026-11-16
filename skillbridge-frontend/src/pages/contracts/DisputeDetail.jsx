import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { disputesApi } from '../../api/disputes';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';

const STATUS_COLOR = {
  OPEN: 'text-amber-400 bg-amber-500/10 border-amber-500/30',
  UNDER_REVIEW: 'text-sky-400 bg-sky-500/10 border-sky-500/30',
  RESOLVED_FOR_CLIENT: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30',
  RESOLVED_FOR_FREELANCER: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30',
  WITHDRAWN: 'text-slate-400 bg-slate-500/10 border-slate-500/30',
  ESCALATED: 'text-rose-400 bg-rose-500/10 border-rose-500/30',
};

const TERMINAL = ['RESOLVED_FOR_CLIENT', 'RESOLVED_FOR_FREELANCER', 'WITHDRAWN'];

export default function DisputeDetail() {
  const { disputeId } = useParams();
  const qc = useQueryClient();
  const toast = useToast();
  const { user } = useAuth();

  const { data: d, isLoading, isError, error } = useQuery({
    queryKey: ['dispute', disputeId],
    queryFn: () => disputesApi.get(disputeId),
  });

  const [message, setMessage] = useState('');
  const [evidence, setEvidence] = useState({ evidenceType: 'LINK', url: '', description: '' });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['dispute', disputeId] });
    qc.invalidateQueries({ queryKey: ['contract-disputes'] });
  };

  const sendMessage = useMutation({
    mutationFn: () => disputesApi.addMessage(disputeId, message),
    onSuccess: () => { setMessage(''); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Failed to send'),
  });

  const sendEvidence = useMutation({
    mutationFn: () => disputesApi.addEvidence(disputeId, evidence),
    onSuccess: () => { setEvidence({ evidenceType: 'LINK', url: '', description: '' }); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Failed to attach evidence'),
  });

  const withdraw = useMutation({
    mutationFn: () => disputesApi.withdraw(disputeId),
    onSuccess: () => { toast.success('Dispute withdrawn'); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Failed'),
  });

  if (isLoading) return <div className="p-6 text-slate-400">Loading dispute…</div>;
  if (isError) return <div className="p-6 text-rose-400">{error?.response?.data?.message || 'Failed to load'}</div>;
  if (!d) return <div className="p-6">Not found.</div>;

  const isTerminal = TERMINAL.includes(d.status);
  const isOpener = user?.id === d.openedByUserId;

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Dispute #{d.id}</h1>
          <p className="text-sm text-slate-400">
            Contract: {d.contractTitle} {d.milestoneId && `· Milestone #${d.milestoneId}`}
          </p>
        </div>
        <Link to={`/contracts/${d.contractId}`} className="text-sm text-indigo-400 hover:underline">
          ← Back to contract
        </Link>
      </header>

      <div className="rounded-lg border border-slate-800 p-4">
        <div className="mb-2 flex items-center gap-2">
          <span className={`rounded-full border px-2 py-0.5 text-xs ${STATUS_COLOR[d.status]}`}>
            {d.status.replace(/_/g, ' ')}
          </span>
          <span className="text-xs text-slate-500">
            opened by {d.openedByName} · {new Date(d.createdAt).toLocaleString()}
          </span>
        </div>
        <p className="text-sm text-slate-200 whitespace-pre-line">{d.reason}</p>

        {d.resolution && (
          <div className="mt-3 rounded border border-slate-700 bg-slate-900/50 p-3">
            <p className="mb-1 text-xs text-slate-400">Resolution by {d.resolvedByName || 'admin'}</p>
            <p className="text-sm text-slate-200 whitespace-pre-line">{d.resolution}</p>
          </div>
        )}

        {!isTerminal && isOpener && (
          <button
            onClick={() => window.confirm('Withdraw this dispute?') && withdraw.mutate()}
            className="mt-3 text-xs text-rose-400 hover:underline"
          >
            Withdraw dispute
          </button>
        )}
      </div>

      {/* Evidence */}
      <section>
        <h2 className="mb-2 font-semibold">Evidence ({d.evidence.length})</h2>
        <div className="space-y-2">
          {d.evidence.map((e) => (
            <div key={e.id} className="rounded border border-slate-800 p-3 text-sm">
              <div className="mb-1 flex items-center justify-between">
                <span className="text-xs text-slate-400">
                  {e.evidenceType} · {e.uploadedByName}
                </span>
                <span className="text-xs text-slate-500">{new Date(e.createdAt).toLocaleString()}</span>
              </div>
              <a href={e.url} target="_blank" rel="noopener noreferrer" className="text-indigo-400 break-all hover:underline">
                {e.url}
              </a>
              {e.description && <p className="mt-1 text-slate-400">{e.description}</p>}
            </div>
          ))}
        </div>

        {!isTerminal && (
          <div className="mt-3 rounded border border-slate-800 p-3 space-y-2">
            <div className="flex gap-2">
              <select
                value={evidence.evidenceType}
                onChange={(e) => setEvidence({ ...evidence, evidenceType: e.target.value })}
                className="rounded border border-slate-700 bg-slate-900 px-2 py-1 text-sm"
              >
                <option value="LINK">Link</option>
                <option value="SCREENSHOT">Screenshot URL</option>
                <option value="FILE">File URL</option>
                <option value="OTHER">Other</option>
              </select>
              <input
                placeholder="https://…"
                value={evidence.url}
                onChange={(e) => setEvidence({ ...evidence, url: e.target.value })}
                className="flex-1 rounded border border-slate-700 bg-slate-900 px-2 py-1 text-sm"
              />
            </div>
            <input
              placeholder="Description (optional)"
              value={evidence.description}
              onChange={(e) => setEvidence({ ...evidence, description: e.target.value })}
              className="w-full rounded border border-slate-700 bg-slate-900 px-2 py-1 text-sm"
            />
            <button
              disabled={!evidence.url || sendEvidence.isPending}
              onClick={() => sendEvidence.mutate()}
              className="rounded bg-indigo-600 px-3 py-1 text-sm disabled:opacity-50"
            >
              {sendEvidence.isPending ? 'Attaching…' : 'Attach evidence'}
            </button>
          </div>
        )}
      </section>

      {/* Thread */}
      <section>
        <h2 className="mb-2 font-semibold">Messages ({d.messages.length})</h2>
        <div className="space-y-2">
          {d.messages.map((m) => (
            <div key={m.id} className={`rounded border p-3 text-sm ${m.isFromAdmin ? 'border-indigo-500/30 bg-indigo-500/5' : 'border-slate-800'}`}>
              <div className="mb-1 flex items-center justify-between text-xs text-slate-400">
                <span>
                  {m.authorName}{m.isFromAdmin && <span className="ml-1 rounded bg-indigo-500/20 px-1 py-0.5 text-[10px] text-indigo-300">ADMIN</span>}
                </span>
                <span>{new Date(m.createdAt).toLocaleString()}</span>
              </div>
              <p className="text-slate-200 whitespace-pre-line">{m.message}</p>
            </div>
          ))}
        </div>

        {!isTerminal && (
          <div className="mt-3 space-y-2">
            <textarea
              rows={3}
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Add a message…"
              className="w-full rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm"
            />
            <button
              disabled={!message.trim() || sendMessage.isPending}
              onClick={() => sendMessage.mutate()}
              className="rounded bg-indigo-600 px-3 py-1 text-sm disabled:opacity-50"
            >
              {sendMessage.isPending ? 'Sending…' : 'Send message'}
            </button>
          </div>
        )}
      </section>
    </div>
  );
}
