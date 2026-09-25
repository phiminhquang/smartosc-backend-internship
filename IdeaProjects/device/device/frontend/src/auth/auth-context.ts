import { createContext } from 'react'
import type { LoginRequest } from '../types/api'

export interface AuthContextValue {
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
