import { motion } from 'framer-motion'
import { Loader2 } from 'lucide-react'

export default function LoadingButton({ loading, disabled, children, className = '', ...props }) {
  return (
    <motion.button
      whileHover={!loading && !disabled ? { y: -1 } : undefined}
      whileTap={!loading && !disabled ? { scale: 0.98 } : undefined}
      disabled={loading || disabled}
      aria-busy={loading}
      className={`flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 text-sm font-bold text-white shadow-lg shadow-brand-500/20 transition-colors hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60 ${className}`}
      {...props}
    >
      {loading && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
      {children}
    </motion.button>
  )
}
