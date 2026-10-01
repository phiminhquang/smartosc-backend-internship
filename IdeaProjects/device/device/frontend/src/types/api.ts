export interface ApiResponse<T> {
  code: number
  message?: string
  result?: T
}

export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
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

export interface PasswordResetRequest {
  email: string
}

export interface PasswordResetConfirmRequest {
  token: string
  newPassword: string
}

export type DeviceCategory = 'LAPTOP' | 'MONITOR' | 'PHONE'
export type DeviceState = 'AVAILABLE' | 'ASSIGNED' | 'UNDER_REPAIR'

export interface DeviceResponse {
  id: string
  category: DeviceCategory
  serialNumber: string
  name: string
  model: string
  description?: string
  state: DeviceState
  updatedBy?: string
  updatedTime?: string
}

export interface DeviceCreationRequest {
  category: DeviceCategory
  name: string
  model: string
  description?: string
}

export interface DeviceUpdateRequest {
  name: string
  model: string
  description?: string
}

export interface DeviceSearchParams {
  keyword?: string
  state?: DeviceState
  category?: DeviceCategory
  page?: number
  size?: number
}
