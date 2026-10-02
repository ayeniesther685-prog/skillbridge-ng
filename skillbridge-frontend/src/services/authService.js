import api, { extractTokens, tokenStorage } from './api.js'

/** Turn any axios error into a safe, user-facing message. Never exposes backend internals. */
export function getErrorMessage(error, context = 'login') {
  if (!error.response) return 'Unable to connect to SkillBridge. Please try again.'
  const status = error.response.status
  switch (status) {
    case 400:
      return context === 'login'
        ? 'Invalid email or password.'
        : 'Please check the details you entered and try again.'
    case 401:
      return 'Invalid email or password.'
    case 403:
      return 'You do not have permission to do that.'
    case 409:
      return 'An account with this email already exists.'
    default:
      return status >= 500
        ? 'Something went wrong on our side. Please try again shortly.'
        : 'Something went wrong. Please try again.'
  }
}

const pickUser = (data) => data?.user ?? null

export const authService = {
  // POST /api/auth/register -> 201 AuthResponse (user is logged in immediately)
  async register({ firstName, lastName, email, password, phone, roles }) {
    const body = { firstName, lastName, email, password, roles }
    if (phone) body.phone = phone
    const { data } = await api.post('/api/auth/register', body)
    const tokens = extractTokens(data)
    if (tokens.accessToken) tokenStorage.set(tokens, true)
    return { tokens, user: pickUser(data) }
  },

  async login({ email, password, remember }) {
    const { data } = await api.post('/api/auth/login', { email, password })
    const tokens = extractTokens(data)
    if (!tokens.accessToken) throw new Error('Login response missing access token')
    tokenStorage.set(tokens, remember)
    return { tokens, user: pickUser(data) }
  },

  async me() {
    const { data } = await api.get('/api/auth/me')
    return data
  },

  async logout() {
    const refreshToken = tokenStorage.getRefresh()
    try {
      // Backend requires { refreshToken } (@Valid); returns 204. Skip if none stored.
      if (refreshToken) await api.post('/api/auth/logout', { refreshToken })
    } finally {
      tokenStorage.clear()
    }
  },
}
