import type {
  ApiResponse,
  AuthenticationResult,
  LoginRequest,
} from '../types/api'
import { http } from './http'

export async function authenticate(
  request: LoginRequest,
): Promise<AuthenticationResult> {
  const response = await http.post<ApiResponse<AuthenticationResult>>(
    '/auth/token',
    request,
  )

  if (!response.data.result?.token) {
    throw new Error('Máy chủ không trả về access token.')
  }

  return response.data.result
}
