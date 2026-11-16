import { useState } from 'react';

export default function DisputeOpenForm({ onSubmit, onCancel }) {
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (reason.trim().length < 20) return;
    setSubmitting(true);
    try {
      await onSubmit({ reason: reason.trim() });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <label className="mb-1 block text-xs text-slate-400">
          What is this dispute about? (minimum 20 characters)
        </label>
        <textarea
          rows={5}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          className="w-full rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm"
          placeholder="Describe the issue precisely — what was agreed, what was delivered, what went wrong."
        />
        <p className="mt-1 text-xs text-slate-500">{reason.length} / 2000</p>
      </div>
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={reason.trim().length < 20 || submitting}
          className="rounded bg-rose-600 px-4 py-2 text-sm font-medium hover:bg-rose-500 disabled:opacity-50"
        >
          {submitting ? 'Opening…' : 'Open dispute'}
        </button>
        <button type="button" onClick={onCancel} className="rounded border border-slate-700 px-4 py-2 text-sm">
          Cancel
        </button>
      </div>
    </form>
  );
}
