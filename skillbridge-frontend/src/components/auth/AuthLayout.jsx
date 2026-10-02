import { motion } from 'framer-motion'
import AuthBrandPanel from './AuthBrandPanel.jsx'
import Logo from '../common/Logo.jsx'

export default function AuthLayout({ variant, children }) {
  return (
    <div className="grid min-h-screen bg-ink-950 lg:grid-cols-2">
      <AuthBrandPanel variant={variant} />
      <main className="flex flex-col items-center justify-center px-4 py-8 sm:px-8">
        <Logo className="mb-8 lg:hidden" />
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="w-full max-w-md rounded-3xl border border-line bg-ink-900/80 p-6 shadow-2xl shadow-black/40 sm:p-9"
        >
          {children}
        </motion.div>
      </main>
    </div>
  )
}
