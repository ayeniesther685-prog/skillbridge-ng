import { ShieldCheck, ShieldAlert, ShieldQuestion, ShieldX } from 'lucide-react';

const STATUS_CONFIG = {
  VERIFIED:   { icon: ShieldCheck,    color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30', label: 'Verified Provider' },
  PENDING:    { icon: ShieldQuestion, color: 'text-amber-400 bg-amber-500/10 border-amber-500/30',       label: 'Verification Pending' },
  UNVERIFIED: { icon: ShieldQuestion, color: 'text-slate-400 bg-slate-500/10 border-slate-500/30',       label: 'Unverified Provider' },
  REJECTED:   { icon: ShieldX,        color: 'text-rose-400 bg-rose-500/10 border-rose-500/30',           label: 'Verification Rejected' },
  SUSPENDED:  { icon: ShieldAlert,    color: 'text-orange-400 bg-orange-500/10 border-orange-500/30',     label: 'Verification Suspended' },
  EXPIRED:    { icon: ShieldAlert,    color: 'text-orange-400 bg-orange-500/10 border-orange-500/30',     label: 'Verification Expired' },
};

export default function VerificationBadge({ status = 'UNVERIFIED', compact = false }) {
  const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.UNVERIFIED;
  const Icon = cfg.icon;
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium ${cfg.color}`}>
      <Icon className="h-3.5 w-3.5" />
      {!compact && cfg.label}
    </span>
  );
}