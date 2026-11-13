import { useState } from 'react'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { normalizeError } from '../api/errors'
import { useToast } from '../context/ToastContext'

// Paginated list helper: returns the query plus page state
export function usePaged(key, fn, params = {}, size = 10) {
  const [page, setPage] = useState(0)
  const q = useQuery({ queryKey: [...key, params, page, size], queryFn: () => fn({ ...params, page, size }), placeholderData: keepPreviousData })
  return { q, page, setPage }
}

// Mutation helper: toast on success/error, invalidate query keys
export function useSave(fn, { invalidate = [], success, onSuccess } = {}) {
  const qc = useQueryClient()
  const toast = useToast()
  return useMutation({
    mutationFn: fn,
    onSuccess: (data, vars) => {
      invalidate.forEach((k) => qc.invalidateQueries({ queryKey: k }))
      if (success) toast.success(success)
      onSuccess?.(data, vars)
    },
    onError: (err) => toast.error(normalizeError(err).message),
  })
}
