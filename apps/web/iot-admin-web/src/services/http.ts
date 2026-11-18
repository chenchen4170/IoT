import axios, { AxiosError } from 'axios'
import { useApiError } from '../stores/apiError'
import { clearToken, getToken } from './auth'
import type { ApiResponse } from './types'

type ApiStatus = Pick<ApiResponse<unknown>, 'code' | 'msg'>

function isApiResponse(data: unknown): data is ApiStatus {
  return typeof data === 'object' && data !== null && 'code' in data &&
    typeof data.code === 'number' && 'msg' in data && typeof data.msg === 'string'
}

function hasFailedCode(data: unknown): data is ApiStatus {
  return isApiResponse(data) && data.code !== 200 && data.code !== 201
}

export function isHandledApiError(error: unknown): boolean {
  return axios.isAxiosError(error) &&
    !!error.response &&
    (error.response.status === 401 || hasFailedCode(error.response.data))
}

let onUnauthorized: (() => void) | undefined

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

function handleApiError(data: unknown, status: number) {
  if (hasFailedCode(data)) {
    useApiError.getState().show(data.msg)
  }
  if (status === 401 || (isApiResponse(data) && data.code === 401)) {
    clearToken()
    onUnauthorized?.()
  }
}

export const http = axios.create({
  baseURL: '/api',
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token && config.url !== '/admin/user/login') {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    if (hasFailedCode(response.data)) {
      handleApiError(response.data, response.status)
      return Promise.reject(
        new AxiosError(response.data.msg, 'ERR_BAD_RESPONSE', response.config, response.request, response),
      )
    }
    return response
  },
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response) {
      handleApiError(error.response.data, error.response.status)
    }
    return Promise.reject(error)
  },
)
