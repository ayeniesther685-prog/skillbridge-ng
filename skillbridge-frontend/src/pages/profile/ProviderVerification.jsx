import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { providerVerificationApi } from '../../api/providerVerification';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import Textarea from '../../components/ui/Textarea';
import VerificationBadge from '../../components/ui/VerificationBadge';

const PROVIDER_TYPES = [
  { value: 'EMPLOYER', label: 'Employer' },
  { value: 'RECRUITMENT_AGENCY', label: 'Recruitment Agency' },
  { value: 'TRAINING_PROVIDER', label: 'Training Provider' },
];

export default function ProviderVerification() {
  const qc = useQueryClient();
  const { data, isLoading, isError } = useQuery({
    queryKey: ['provider-verification', 'me'],
    queryFn: async () => {
      try {
        return (await providerVerificationApi.getMine()).data;
      } catch (e) {
        if (e?.response?.status === 404) return null;
        throw e;
      }
    },
  });

  const [form, setForm] = useState({
    providerType: 'EMPLOYER',
    legalName: '',
    cacRegistrationNumber: '',
    officialEmail: '',
    officialPhone: '',
    physicalAddress: '',
    website: '',
    linkedinUrl: '',
    authorizedRepName: '',
    authorizedRepRole: '',
  });

  useEffect(() => {
    if (data) {
      setForm({
        providerType: data.providerType || 'EMPLOYER',
        legalName: data.legalName || '',
        cacRegistrationNumber: data.cacRegistrationNumber || '',
        officialEmail: data.officialEmail || '',
        officialPhone: data.officialPhone || '',
        physicalAddress: data.physicalAddress || '',
        website: data.website || '',
        linkedinUrl: data.linkedinUrl || '',
        authorizedRepName: data.authorizedRepName || '',
        authorizedRepRole: data.authorizedRepRole || '',
      });
    }
  }, [data]);

  const submit = useMutation({
    mutationFn: (payload) => providerVerificationApi.submit(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['provider-verification'] }),
  });

  if (isLoading) return <div className="p-6">Loading…</div>;
  if (isError) return <div className="p-6 text-rose-400">Failed to load verification profile.</div>;

  const status = data?.status || 'UNVERIFIED';
  const locked = status === 'PENDING' || status === 'VERIFIED';

  return (
    <div className="mx-auto max-w-3xl space-y-6 p-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold">Provider Verification</h1>
        <p className="text-slate-400">
          Verify your business identity to post jobs and hire talent.
        </p>
        <VerificationBadge status={status} />
      </header>

      {data?.adminNotes && (
        <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 p-4 text-sm text-amber-300">
          <strong>Admin notes:</strong> {data.adminNotes}
        </div>
      )}

      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          submit.mutate(form);
        }}
      >
        <Select
          label="Provider type"
          value={form.providerType}
          onChange={(v) => setForm({ ...form, providerType: v })}
          options={PROVIDER_TYPES}
          disabled={locked}
        />

        <Input label="Legal name" value={form.legalName} onChange={(e) => setForm({ ...form, legalName: e.target.value })} disabled={locked} required />
        <Input label="CAC registration number" value={form.cacRegistrationNumber} onChange={(e) => setForm({ ...form, cacRegistrationNumber: e.target.value })} disabled={locked} />
        <Input label="Official email" type="email" value={form.officialEmail} onChange={(e) => setForm({ ...form, officialEmail: e.target.value })} disabled={locked} required />
        <Input label="Official phone" value={form.officialPhone} onChange={(e) => setForm({ ...form, officialPhone: e.target.value })} disabled={locked} required />
        <Textarea label="Physical address" value={form.physicalAddress} onChange={(e) => setForm({ ...form, physicalAddress: e.target.value })} disabled={locked} required rows={3} />
        <Input label="Website" value={form.website} onChange={(e) => setForm({ ...form, website: e.target.value })} disabled={locked} />
        <Input label="LinkedIn company page" value={form.linkedinUrl} onChange={(e) => setForm({ ...form, linkedinUrl: e.target.value })} disabled={locked} />
        <Input label="Authorized representative name" value={form.authorizedRepName} onChange={(e) => setForm({ ...form, authorizedRepName: e.target.value })} disabled={locked} required />
        <Input label="Authorized representative role" value={form.authorizedRepRole} onChange={(e) => setForm({ ...form, authorizedRepRole: e.target.value })} disabled={locked} required />

        {submit.isError && (
          <div className="text-sm text-rose-400">
            {submit.error?.response?.data?.message || 'Submission failed.'}
          </div>
        )}

        <Button type="submit" disabled={locked || submit.isPending}>
          {submit.isPending ? 'Submitting…' : 'Submit for verification'}
        </Button>
      </form>
    </div>
  );
}