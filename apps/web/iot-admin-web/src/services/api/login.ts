import { http } from '../http'
import type { ApiResponse } from '../types'

export type LoginRequest = {
  username: string
  password: string
}

type LoginData = {
  id: number
  username: string
  token: string
  refreshToken: string
}

export async function login(values: LoginRequest): Promise<LoginData> {
  const response = await http.post<ApiResponse<LoginData>>('/admin/user/login', values)
  const data = response.data.data
  if (!data || typeof data.token !== 'string' || !data.token.trim()) {
    throw new Error('登录响应缺少 token')
  }
  return data
}
