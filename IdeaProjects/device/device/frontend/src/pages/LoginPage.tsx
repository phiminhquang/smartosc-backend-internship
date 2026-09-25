import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { getErrorMessage } from '../services/http'

interface LoginLocationState {
  from?: {
    pathname?: string
  }
}

export function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSubmitting(true)

    try {
      await login({ email, password })
      const state = location.state as LoginLocationState | null
      navigate(state?.from?.pathname ?? '/', { replace: true })
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="brand-panel">
        <div className="brand-mark" aria-hidden="true">
          DM
        </div>
        <p className="eyebrow">DEVICE MANAGEMENT</p>
        <h1>Quản lý toàn bộ vòng đời thiết bị.</h1>
        <p className="brand-copy">
          Theo dõi thiết bị, cấp phát, gia hạn và sửa chữa trong cùng một hệ
          thống.
        </p>
        <div className="feature-list" aria-label="Chức năng chính">
          <span>Thiết bị</span>
          <span>Cấp phát</span>
          <span>Sửa chữa</span>
        </div>
      </section>

      <section className="form-panel">
        <form className="login-card" onSubmit={handleSubmit}>
          <div>
            <p className="eyebrow">WELCOME BACK</p>
            <h2>Đăng nhập</h2>
            <p className="form-description">
              Sử dụng tài khoản đã được tạo trong hệ thống.
            </p>
          </div>

          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="admin@gmail.com"
              autoComplete="email"
              required
            />
          </label>

          <label>
            Mật khẩu
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Nhập mật khẩu"
              autoComplete="current-password"
              required
            />
          </label>

          {error && <p className="error-message">{error}</p>}

          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>

          <p className="learning-note">
            Form này gửi JSON tới <code>POST /api/auth/token</code>.
          </p>
        </form>
      </section>
    </main>
  )
}
