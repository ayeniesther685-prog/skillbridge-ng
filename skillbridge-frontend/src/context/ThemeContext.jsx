import { createContext, useContext, useEffect, useState } from 'react'
const Ctx = createContext(null)
export const useTheme = () => useContext(Ctx)
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => { try { return localStorage.getItem('sb_theme') || 'dark' } catch { return 'dark' } })
  useEffect(() => {
    document.documentElement.dataset.theme = theme
    try { localStorage.setItem('sb_theme', theme) } catch {}
  }, [theme])
  return <Ctx.Provider value={{ theme, toggle: () => setTheme((t) => (t === 'dark' ? 'light' : 'dark')) }}>{children}</Ctx.Provider>
}
