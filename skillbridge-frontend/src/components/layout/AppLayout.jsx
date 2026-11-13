import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import Sidebar, { MobileSidebar } from './Sidebar'
import Topbar from './Topbar'

export default function AppLayout() {
  const [open, setOpen] = useState(false)
  const { pathname } = useLocation()

  return (
    <div className="flex min-h-screen bg-bg">
      <Sidebar />
      <MobileSidebar open={open} onClose={() => setOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar onMenu={() => setOpen(true)} />
        <motion.main
          key={pathname}
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, ease: [0.22, 1, 0.36, 1] }}
          className="mx-auto w-full max-w-6xl flex-1 p-4 sm:p-6 lg:p-8"
        >
          <Outlet />
        </motion.main>
      </div>
    </div>
  )
}