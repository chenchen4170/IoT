import { useEffect } from 'react'
import { Alert } from 'antd'
import { useApiError } from '../stores/apiError'

export function ApiErrorAlert() {
  const notice = useApiError((state) => state.notice)
  const clear = useApiError((state) => state.clear)

  useEffect(() => {
    if (!notice) return
    const timer = window.setTimeout(() => clear(notice.id), 2000)
    return () => window.clearTimeout(timer)
  }, [notice, clear])

  if (!notice) return null

  return (
    <div className="api-error-alert">
      <Alert title={notice.title} type="error" showIcon />
    </div>
  )
}
