const v = (n) => `rgb(var(--${n}) / <alpha-value>)`
export default {
  darkMode: ['class', '[data-theme="dark"]'],
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        bg: v('bg'), surface: v('surface'), card: v('card'), line: v('line'),
        primary: v('primary'), accent: v('accent'), warning: v('warning'),
        success: v('success'), danger: v('danger'), fg: v('fg'), muted: v('muted'),
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'ui-monospace', 'monospace'],
      },
      borderRadius: { DEFAULT: '8px', lg: '12px' },
    },
  },
  plugins: [],
}
