import { useEffect } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { X } from 'lucide-react'
// Modal on desktop, full-screen sheet on mobile
export default function Dialog({ open, onClose, title, children }) {
  useEffect(() => {
    if (!open) return
    const h = (e) => e.key === 'Escape' && onClose()
    document.addEventListener('keydown', h)
    return () => document.removeEventListener('keydown', h)
  }, [open, onClose])
  return (
    <AnimatePresence>
      {open && (
        <div className="fixed inset-0 z-[90] flex items-stretch justify-center sm:items-center sm:p-4" role="dialog" aria-modal="true" aria-label={title}>
          <div className="absolute inset-0 bg-black/60" onClick={onClose} />
          <motion.div initial={{ scale: 0.96, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ opacity: 0 }} transition={{ duration: 0.15 }}
            className="relative flex w-full flex-col overflow-hidden bg-surface sm:max-h-[90vh] sm:max-w-lg sm:rounded-lg sm:border">
            <div className="flex items-center justify-between border-b px-5 py-4">
              <h2 className="font-semibold">{title}</h2>
              <button onClick={onClose} className="rounded p-2 hover:bg-fg/5" aria-label="Close"><X className="h-4 w-4" /></button>
            </div>
            <div className="flex-1 overflow-y-auto p-5">{children}</div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  )
}
