import { createContext, useCallback, useContext, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { CheckCircle2, AlertCircle, Info } from 'lucide-react'
const Ctx = createContext(null)
export const useToast = () => useContext(Ctx)
const icons = { success: CheckCircle2, error: AlertCircle, info: Info }
const tone = { success: 'text-success', error: 'text-danger', info: 'text-accent' }
export function ToastProvider({ children }) {
  const [items, setItems] = useState([])
  const push = useCallback((type, message) => {
    const id = crypto.randomUUID()
    setItems((x) => [...x, { id, type, message }])
    setTimeout(() => setItems((x) => x.filter((i) => i.id !== id)), 4000)
  }, [])
  const api = { success: (m) => push('success', m), error: (m) => push('error', m), info: (m) => push('info', m) }
  return (
    <Ctx.Provider value={api}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2" role="status" aria-live="polite">
        <AnimatePresence>
          {items.map((t) => {
            const Icon = icons[t.type]
            return (
              <motion.div key={t.id} initial={{ x: 40, opacity: 0 }} animate={{ x: 0, opacity: 1 }} exit={{ opacity: 0 }} transition={{ duration: 0.15 }}
                className="flex items-start gap-3 rounded-lg border bg-surface p-3 text-sm shadow-lg">
                <Icon className={`mt-0.5 h-4 w-4 shrink-0 ${tone[t.type]}`} />
                <span>{t.message}</span>
              </motion.div>
            )
          })}
        </AnimatePresence>
      </div>
    </Ctx.Provider>
  )
}
