import { useEffect, useRef, type ReactNode } from 'react'
import { CloseIcon } from './Icons'

export interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title: string
  titleId?: string
  children: ReactNode
}

export function Modal({
  isOpen,
  onClose,
  title,
  titleId = 'modal-title',
  children,
}: ModalProps) {
  const cardRef = useRef<HTMLDivElement>(null)
  const previousActiveElement = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (!isOpen) return

    // Save previously focused element to restore focus upon close
    previousActiveElement.current = document.activeElement as HTMLElement | null

    // Lock background scrolling
    const originalOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const focusableSelectors =
      'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

    // Focus the first focusable element inside the modal
    const focusTimer = setTimeout(() => {
      if (cardRef.current) {
        const focusables = cardRef.current.querySelectorAll<HTMLElement>(focusableSelectors)
        const firstFocusable = focusables[0]
        if (firstFocusable) {
          firstFocusable.focus()
        } else {
          cardRef.current.focus()
        }
      }
    }, 40)

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        event.preventDefault()
        onClose()
        return
      }

      if (event.key === 'Tab' && cardRef.current) {
        const focusables = cardRef.current.querySelectorAll<HTMLElement>(focusableSelectors)
        if (focusables.length === 0) return

        const first = focusables[0]
        const last = focusables[focusables.length - 1]

        if (event.shiftKey) {
          if (document.activeElement === first) {
            event.preventDefault()
            last.focus()
          }
        } else {
          if (document.activeElement === last) {
            event.preventDefault()
            first.focus()
          }
        }
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => {
      clearTimeout(focusTimer)
      document.body.style.overflow = originalOverflow
      window.removeEventListener('keydown', handleKeyDown)
      if (previousActiveElement.current) {
        previousActiveElement.current.focus()
      }
    }
  }, [isOpen, onClose])

  if (!isOpen) return null

  return (
    <div
      className="modal-backdrop"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose()
        }
      }}
    >
      <div
        className="modal-card"
        ref={cardRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
      >
        <div className="modal-header">
          <h3 id={titleId}>{title}</h3>
          <button
            type="button"
            className="close-button"
            aria-label={`Đóng hộp thoại ${title}`}
            onClick={onClose}
          >
            <CloseIcon />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
