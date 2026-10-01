import type {
  ApiResponse,
  AuthenticationResult,
  LoginRequest,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
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

export async function requestPasswordReset(
  request: PasswordResetRequest,
): Promise<string> {
  const response = await http.post<ApiResponse<void>>(
    '/auth/password-reset/request',
    request,
  )

  return (
    response.data.message ??
    'Nếu email tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi.'
  )
}

export async function confirmPasswordReset(
  request: PasswordResetConfirmRequest,
): Promise<string> {
  const response = await http.post<ApiResponse<void>>(
    '/auth/password-reset/confirm',
    request,
  )

  return response.data.message ?? 'Đặt lại mật khẩu thành công.'
}
