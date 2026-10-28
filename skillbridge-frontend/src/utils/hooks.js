import { useCallback, useEffect, useRef, useState } from 'react'

/** Run an async fn on mount / when deps change. Returns { data, loading, error, reload, setData }. */
export function useFetch(fn, deps = [], enabled = true) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(enabled)
  const [error, setError] = useState(null)
  const fnRef = useRef(fn)
  fnRef.current = fn
  const run = useCallback(async (silent = false) => {
    if (!silent) setLoading(true)
    setError(null)
    try { setData(await fnRef.current()) } catch (e) { setError(e) } finally { setLoading(false) }
  }, [])
  useEffect(() => {
    if (enabled) run()
    else setLoading(false)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, ...deps])
  return { data, loading, error, reload: run, setData }
}
