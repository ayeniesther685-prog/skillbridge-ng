import { Moon, Sun } from 'lucide-react'
import { useTheme } from '../../context/ThemeContext'
export default function ThemeToggle() {
  const { theme, toggle } = useTheme()
  return (
    <button onClick={toggle} className="rounded p-2 text-muted hover:bg-fg/5 hover:text-fg" aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}>
      {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </button>
  )
}
