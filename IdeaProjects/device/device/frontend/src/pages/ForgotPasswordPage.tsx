import { useState, type FormEvent } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { requestPasswordReset } from '../services/authService'
import { getErrorMessage } from '../services/http'

export function ForgotPasswordPage() {
  const { isAuthenticated } = useAuth()
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSuccessMessage('')
    setSubmitting(true)

    try {
      const message = await requestPasswordReset({ email })
      setSuccessMessage(message)
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
        <h1>Quên mật khẩu?</h1>
        <p className="brand-copy">
          Nhập địa chỉ email tài khoản của bạn để nhận liên kết đặt lại mật khẩu an toàn.
        </p>
      </section>

      <section className="form-panel">
        <form className="login-card" onSubmit={handleSubmit}>
          <div>
            <p className="eyebrow">PASSWORD RESET</p>
            <h2>Khôi phục mật khẩu</h2>
            <p className="form-description">
              Chúng tôi sẽ gửi một liên kết đặt lại mật khẩu có hiệu lực trong 15 phút.
            </p>
          </div>

          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="user@example.com"
              autoComplete="email"
              required
            />
          </label>

          {error && <p className="error-message">{error}</p>}
          {successMessage && <p className="success-message">{successMessage}</p>}

          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Đang gửi yêu cầu...' : 'Gửi liên kết đặt lại mật khẩu'}
          </button>

          <p className="auth-footer-links">
            <Link to="/login" className="text-link">
              Quay lại đăng nhập
            </Link>
          </p>
        </form>
      </section>
    </main>
  )
}
