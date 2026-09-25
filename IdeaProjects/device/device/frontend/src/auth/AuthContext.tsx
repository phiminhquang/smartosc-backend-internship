import { useMemo, useState, type ReactNode } from 'react'
import { authenticate } from '../services/authService'
import { TOKEN_STORAGE_KEY } from '../services/http'
import type { LoginRequest } from '../types/api'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() =>
    sessionStorage.getItem(TOKEN_STORAGE_KEY),
  )

  async function login(request: LoginRequest) {
    const result = await authenticate(request)
    sessionStorage.setItem(TOKEN_STORAGE_KEY, result.token)
    setToken(result.token)
  }

  function logout() {
    sessionStorage.removeItem(TOKEN_STORAGE_KEY)
    setToken(null)
  }

  const value = useMemo(
    () => ({ isAuthenticated: Boolean(token), login, logout }),
    [token],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
