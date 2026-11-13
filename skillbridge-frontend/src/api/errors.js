export function normalizeError(err) {
  const res = err?.response
  const data = res?.data || {}
  return {
    status: res?.status ?? 0,
    message: data.message || (res ? 'Something went wrong. Try again.' : 'Cannot reach the server. Check that the backend is running.'),
    fieldErrors: data.fieldErrors || null,
    raw: data,
  }
}
