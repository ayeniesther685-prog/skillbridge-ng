import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from './context/ThemeContext'
import { ToastProvider } from './context/ToastContext'
import { AuthProvider } from './context/AuthContext'
import AppRoutes from './routes/AppRoutes'
const qc = new QueryClient({ defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false, staleTime: 30000 } } })
export default function App() {
  return (
    <QueryClientProvider client={qc}>
      <ThemeProvider><ToastProvider><BrowserRouter><AuthProvider><AppRoutes /></AuthProvider></BrowserRouter></ToastProvider></ThemeProvider>
    </QueryClientProvider>
  )
}
