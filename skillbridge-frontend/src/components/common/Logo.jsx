export default function Logo({ className = '' }) {
  return (
    <div className={`flex items-center gap-2.5 ${className}`}>
      <svg width="34" height="34" viewBox="0 0 34 34" fill="none" aria-hidden="true">
        <rect width="34" height="34" rx="10" fill="#4f46e5" />
        <path d="M7 22c4 0 5-10 10-10s6 10 10 10" stroke="#fff" strokeWidth="2.6" strokeLinecap="round" />
        <circle cx="27" cy="22" r="2.4" fill="#ff8a3d" />
      </svg>
      <span className="text-lg font-extrabold tracking-tight">SkillBridge <span className="text-brand-400">NG</span></span>
    </div>
  )
}
