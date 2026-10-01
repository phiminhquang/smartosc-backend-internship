import { useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { confirmPasswordReset } from '../services/authService'
import { getErrorMessage } from '../services/http'

export function ResetPasswordPage() {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const tokenFromUrl = searchParams.get('token') ?? ''

  const [token, setToken] = useState(tokenFromUrl)
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  useEffect(() => {
    if (tokenFromUrl) {
      navigate('/reset-password', { replace: true })
    }
  }, [navigate, tokenFromUrl])

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSuccessMessage('')

    if (!token.trim()) {
      setError('Mã xác thực token không được để trống.')
      return
    }

    if (newPassword.length < 8 || newPassword.length > 128) {
      setError('Mật khẩu mới phải từ 8 đến 128 ký tự.')
      return
    }

    if (newPassword !== confirmPassword) {
      setError('Xác nhận mật khẩu mới không trùng khớp.')
      return
    }

    setSubmitting(true)

    try {
      const message = await confirmPasswordReset({
        token: token.trim(),
        newPassword,
      })
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
        <h1>Đặt lại mật khẩu mới.</h1>
        <p className="brand-copy">
          Thiết lập mật khẩu an toàn mới cho tài khoản của bạn. Mọi phiên đăng nhập
          cũ trên các thiết bị khác sẽ được tự động đăng xuất.
        </p>
      </section>

      <section className="form-panel">
        <form className="login-card" onSubmit={handleSubmit}>
          <div>
            <p className="eyebrow">NEW PASSWORD</p>
            <h2>Mật khẩu mới</h2>
            <p className="form-description">
              Nhập mã token từ email và mật khẩu mới của bạn.
            </p>
          </div>

          <label>
            Mã Token
            <input
              type="password"
              value={token}
              onChange={(event) => setToken(event.target.value)}
              placeholder="Nhập mã token từ liên kết email"
              autoComplete="off"
              spellCheck={false}
              required
            />
          </label>

          <label>
            Mật khẩu mới
            <input
              type="password"
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
              placeholder="Tối thiểu 8 ký tự"
              autoComplete="new-password"
              required
            />
          </label>

          <label>
            Xác nhận mật khẩu mới
            <input
              type="password"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              placeholder="Nhập lại mật khẩu mới"
              autoComplete="new-password"
              required
            />
          </label>

          {error && <p className="error-message">{error}</p>}
          {successMessage && (
            <div className="success-box">
              <p className="success-message">{successMessage}</p>
              <Link to="/login" className="primary-button inline-button">
                Đăng nhập ngay
              </Link>
            </div>
          )}

          {!successMessage && (
            <button className="primary-button" type="submit" disabled={submitting}>
              {submitting ? 'Đang cập nhật...' : 'Xác nhận đặt lại mật khẩu'}
            </button>
          )}

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
