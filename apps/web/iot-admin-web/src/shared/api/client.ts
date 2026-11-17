import { apiBaseUrl } from '../config/api'

interface ApiResponse<T> {
  code: number
  traceId: string
  msg: string
  data: T
}

export async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    headers: { Accept: 'application/json' },
    signal,
  })
  const body: unknown = await response.json().catch(() => null)
  const result = body as Partial<ApiResponse<T>> | null

  if (!response.ok) {
    throw new Error(result?.msg || `请求失败 (HTTP ${response.status})`)
  }

  if (!result || typeof result.code !== 'number' || !('data' in result)) {
    throw new Error('接口返回了无效数据')
  }

  if (result.code < 200 || result.code >= 300) {
    throw new Error(result.msg || `请求失败 (code ${result.code})`)
  }

  return result.data as T
}
