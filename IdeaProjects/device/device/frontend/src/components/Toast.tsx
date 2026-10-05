import { useEffect } from 'react'
import { CloseIcon } from './Icons'

export interface ToastProps {
  message: string
  type?: 'success' | 'error' | 'info'
  onClose: () => void
  duration?: number
}

export function Toast({
  message,
  type = 'success',
  onClose,
  duration = 3500,
}: ToastProps) {
  useEffect(() => {
    if (!message) return
    const timer = setTimeout(() => {
      onClose()
    }, duration)
    return () => clearTimeout(timer)
  }, [message, duration, onClose])

  if (!message) return null

  return (
    <div
      className={`toast-notification toast-${type}`}
      role="status"
      aria-live="polite"
    >
      <div className="toast-icon">
        {type === 'success' ? (
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
        ) : (
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        )}
      </div>
      <span className="toast-message">{message}</span>
      <button
        type="button"
        className="toast-close"
        aria-label="Đóng thông báo"
        onClick={onClose}
      >
        <CloseIcon width={14} height={14} />
      </button>
    </div>
  )
}
