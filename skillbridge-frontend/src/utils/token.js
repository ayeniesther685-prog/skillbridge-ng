const ACCESS = 'sb_access_token'
const REFRESH = 'sb_refresh_token'

// remember=true -> localStorage (survives browser close); false -> sessionStorage
export const tokenStorage = {
  getAccess: () => localStorage.getItem(ACCESS) ?? sessionStorage.getItem(ACCESS),
  getRefresh: () => localStorage.getItem(REFRESH) ?? sessionStorage.getItem(REFRESH),
  set({ accessToken, refreshToken }, remember) {
    const useLocal = remember === undefined ? !!localStorage.getItem(REFRESH) || !sessionStorage.getItem(REFRESH) : remember
    const store = useLocal ? localStorage : sessionStorage
    const other = useLocal ? sessionStorage : localStorage
    other.removeItem(ACCESS); other.removeItem(REFRESH)
    if (accessToken) store.setItem(ACCESS, accessToken)
    if (refreshToken) store.setItem(REFRESH, refreshToken)
  },
  clear() {
    for (const s of [localStorage, sessionStorage]) { s.removeItem(ACCESS); s.removeItem(REFRESH) }
  },
}
