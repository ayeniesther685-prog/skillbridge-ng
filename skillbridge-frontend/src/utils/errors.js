const FALLBACK = 'Something went wrong. Please try again.'

/** Field errors from backend validation: { field: message } */
export const getFieldErrors = (error) => error?.response?.data?.fieldErrors ?? {}

/** Safe, user-facing message for any Axios error. Never exposes stack traces. */
export function getErrorMessage(error, fallback = FALLBACK) {
  if (!error?.response) {
    return error?.request || error?.code === 'ERR_NETWORK'
      ? 'Unable to connect to SkillBridge. Please try again.'
      : fallback
  }
  const { status, data } = error.response
  const fields = Object.entries(data?.fieldErrors ?? {}).map(([k, v]) => `${k}: ${v}`)
  if (fields.length) return fields.join('\n')
  if (status < 500 && typeof data?.message === 'string' && data.message) return data.message
  if (status === 403) return 'You do not have permission to do that.'
  if (status === 404) return 'We could not find what you were looking for.'
  return fallback
}
