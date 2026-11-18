import axios from 'axios'
import { http } from '../http'
import type { ApiResponse } from '../types'

const testAuthPath = '/admin/test/auth'

export async function getTestAuth() {
  console.log('测试接口请求 GET', `/api${testAuthPath}`)
  try {
    const response = await http.get<ApiResponse<unknown>>(testAuthPath)
    console.log('测试接口响应', response.status, response.data)
    return response
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      console.log('测试接口响应', error.response.status, error.response.data)
    }
    throw error
  }
}
