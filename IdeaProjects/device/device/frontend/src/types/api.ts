export interface ApiResponse<T> {
  code: number
  message?: string
  result?: T
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthenticationResult {
  authenticated: boolean
  token: string
  expiryTime: string
}
