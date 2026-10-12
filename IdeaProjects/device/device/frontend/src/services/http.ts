import axios from 'axios'
import type { ApiResponse } from '../types/api'

export const TOKEN_STORAGE_KEY = 'device_access_token'
export const REQUEST_ID_HEADER = 'X-Request-ID'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_STORAGE_KEY)

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

let lastResponseRequestId: string | null = null

export function getLastResponseRequestId(): string | null {
  return lastResponseRequestId
}

export function extractHeaderRequestId(headers: unknown): string | undefined {
  if (!headers || typeof headers !== 'object') {
    return undefined
  }

  const h = headers as Record<string, unknown> & { get?: (name: string) => unknown }
  const val =
    (typeof h.get === 'function' ? h.get('x-request-id') : undefined) ??
    h['x-request-id'] ??
    h['X-Request-ID'] ??
    h['x-request-Id']

  if (typeof val === 'string' && val.trim()) {
    return val.trim()
  }

  return undefined
}

export function getRequestId(error: unknown): string | undefined {
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    const bodyRequestId = error.response?.data?.requestId
    if (typeof bodyRequestId === 'string' && bodyRequestId.trim()) {
      return bodyRequestId.trim()
    }

    return extractHeaderRequestId(error.response?.headers)
  }

  return undefined
}

http.interceptors.response.use(
  (response) => {
    const reqId = extractHeaderRequestId(response.headers)
    if (reqId) {
      lastResponseRequestId = reqId
    }
    return response
  },
  (error) => {
    const reqId = getRequestId(error)
    if (reqId) {
      lastResponseRequestId = reqId
    }
    return Promise.reject(error)
  }
)

export function getErrorMessage(error: unknown): string {
  const requestId = getRequestId(error)

  let baseMessage: string
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    baseMessage = error.response?.data?.message ?? 'Không thể kết nối tới máy chủ.'
  } else if (error instanceof Error) {
    baseMessage = error.message
  } else {
    baseMessage = 'Đã xảy ra lỗi không xác định.'
  }

  if (requestId && !baseMessage.includes(requestId)) {
    return `${baseMessage} (Mã tham chiếu: ${requestId})`
  }

  return baseMessage
}
