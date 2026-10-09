import { test, expect, type Request } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'

const FRONTEND_URL = process.env.FRONTEND_URL || 'http://127.0.0.1:5173'
const BACKEND_URL = process.env.BACKEND_URL || 'http://127.0.0.1:8080'
const MAILPIT_URL = process.env.MAILPIT_URL || 'http://127.0.0.1:8025'

interface MailpitMessageSummary {
  ID: string
  Snippet: string
  Subject: string
  To: Array<{ Address: string; Name: string }>
  Created: string
}

interface MailpitMessageDetail {
  ID: string
  HTML: string
  Text: string
}

function resolveAdminCredentials(): { email: string; initialPass: string } {
  let email = process.env.ADMIN_EMAIL || 'admin@device.local'
  let initialPass = process.env.LOCAL_ADMIN_PASSWORD || process.env.ADMIN_PASSWORD || ''

  if (!initialPass) {
    const envCandidates = [
      path.resolve(process.cwd(), '.env'),
      path.resolve(process.cwd(), '..', '.env'),
      path.resolve(process.cwd(), 'IdeaProjects/device/device/.env'),
    ]
    for (const p of envCandidates) {
      if (fs.existsSync(p)) {
        const text = fs.readFileSync(p, 'utf-8')
        const passMatch = text.match(/^(?:LOCAL_ADMIN_PASSWORD|ADMIN_PASSWORD)=(.+)$/m)
        if (passMatch) {
          initialPass = passMatch[1].trim()
        }
        const emailMatch = text.match(/^(?:ADMIN_EMAIL)=(.+)$/m)
        if (emailMatch) {
          email = emailMatch[1].trim()
        }
        if (initialPass) break
      }
    }
  }

  if (!initialPass) {
    throw new Error('Không tìm thấy mật khẩu admin qua biến môi trường hoặc file .env.')
  }

  return { email, initialPass }
}

async function ensureCooldownExpired(): Promise<void> {
  const listRes = await fetch(`${MAILPIT_URL}/api/v1/messages`).catch(() => null)
  if (listRes && listRes.ok) {
    const data = (await listRes.json()) as { messages: MailpitMessageSummary[] }
    if (data.messages && data.messages.length > 0) {
      const lastMsg = data.messages[0]
      const elapsedSec = (Date.now() - new Date(lastMsg.Created).getTime()) / 1000
      if (elapsedSec < 62) {
        const waitMs = Math.ceil(62 - elapsedSec) * 1000
        await new Promise((r) => setTimeout(r, waitMs))
      }
    }
  }
}

async function clearMailpitMessages(): Promise<void> {
  await fetch(`${MAILPIT_URL}/api/v1/messages`, { method: 'DELETE' }).catch(() => null)
}

async function fetchLatestResetToken(
  recipientEmail: string,
): Promise<{ token: string; rawLink: string }> {
  const maxAttempts = 30
  const delayMs = 1000

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    const listRes = await fetch(`${MAILPIT_URL}/api/v1/messages`)
    if (!listRes.ok) {
      throw new Error(`Không thể kết nối Mailpit API (status ${listRes.status}).`)
    }

    const data = (await listRes.json()) as { messages: MailpitMessageSummary[] }
    const messages = data.messages || []

    const candidate = messages.find((m) =>
      m.To?.some((t) => t.Address.toLowerCase() === recipientEmail.toLowerCase()),
    )

    if (candidate) {
      const detailRes = await fetch(`${MAILPIT_URL}/api/v1/message/${candidate.ID}`)
      if (detailRes.ok) {
        const detail = (await detailRes.json()) as MailpitMessageDetail
        const content = (detail.HTML || '') + ' ' + (detail.Text || '')

        const linkMatch =
          content.match(/href="([^"]*\/reset-password\?token=[a-zA-Z0-9_-]+)"/i) ||
          content.match(/(https?:\/\/[^\s"']+reset-password\?token=[a-zA-Z0-9_-]+)/i)

        if (linkMatch && linkMatch[1]) {
          const rawLink = linkMatch[1]
          const tokenMatch = rawLink.match(/[?&]token=([a-zA-Z0-9_-]+)/)
          if (tokenMatch && tokenMatch[1]) {
            return { token: tokenMatch[1], rawLink }
          }
        }
      }
    }

    await new Promise((resolve) => setTimeout(resolve, delayMs))
  }

  throw new Error(`Quá thời gian chờ nhận email đặt lại mật khẩu trong Mailpit cho ${recipientEmail}.`)
}

test.describe('End-to-End Browser Authentication & Password Reset Flow', () => {
  test('Kiểm tra toàn bộ luồng đăng nhập, quên mật khẩu và bảo mật token trên trình duyệt thật qua DOM', async ({
    page,
    baseURL,
  }) => {
    const origin = baseURL || FRONTEND_URL
    const { email: adminEmail, initialPass } = resolveAdminCredentials()
    const tempNewPass = 'NewSecretAdminPass!2026'

    // 1. Kiểm tra tính sẵn sàng của hệ thống (Fail fast nếu service chưa chạy)
    const backendHealth = await fetch(`${BACKEND_URL}/v3/api-docs`).catch(() => null)
    expect(
      backendHealth && backendHealth.ok,
      'Backend REST API phải phản hồi HTTP 200 tại /v3/api-docs',
    ).toBeTruthy()

    const mailpitHealth = await fetch(`${MAILPIT_URL}/api/v1/info`).catch(() => null)
    expect(
      mailpitHealth && mailpitHealth.ok,
      'Mailpit API phải phản hồi HTTP 200 tại /api/v1/info',
    ).toBeTruthy()

    // 2. Mở trang đăng nhập trên trình duyệt thật qua DOM
    await page.goto(`${origin}/login`)
    await expect(page).toHaveURL(`${origin}/login`)
    await expect(page.locator('form.login-card')).toBeVisible()

    // 3. Xác định trạng thái mật khẩu hiện tại (Hỗ trợ chạy lặp lại an toàn)
    let currentWorkingPass = initialPass
    await page.fill('input[type="email"]', adminEmail)
    await page.fill('input[type="password"]', initialPass)
    await page.click('button[type="submit"]')

    const loginSuccessWithInitial = await page
      .waitForURL(`${origin}/`, { timeout: 3000 })
      .then(() => true)
      .catch(() => false)

    if (loginSuccessWithInitial) {
      await expect(page.locator('header.topbar strong')).toContainText('Hệ thống Quản lý Thiết bị')
      await page.click('button:has-text("Đăng xuất")')
      await expect(page).toHaveURL(`${origin}/login`)
      currentWorkingPass = initialPass
    } else {
      await page.goto(`${origin}/login`)
      await page.fill('input[type="email"]', adminEmail)
      await page.fill('input[type="password"]', tempNewPass)
      await page.click('button[type="submit"]')

      await expect(
        page,
        'Đăng nhập phải thành công với mật khẩu cấu hình hoặc mật khẩu test trước đó.',
      ).toHaveURL(`${origin}/`)

      await page.click('button:has-text("Đăng xuất")')
      await expect(page).toHaveURL(`${origin}/login`)
      currentWorkingPass = tempNewPass
    }

    const targetNewPass = currentWorkingPass === initialPass ? tempNewPass : initialPass
    const oldPassBeforeReset = currentWorkingPass

    // Đảm bảo không dính cooldown từ lần chạy trước và dọn sạch mailbox
    await ensureCooldownExpired()
    await clearMailpitMessages()

    // 4. Chọn "Quên mật khẩu?" trên giao diện
    await page.click('a:has-text("Quên mật khẩu?")')
    await expect(page).toHaveURL(`${origin}/forgot-password`)
    await expect(page.locator('h1')).toContainText('Quên mật khẩu?')

    // 5. Gửi yêu cầu đặt lại mật khẩu cho tài khoản local
    await page.fill('input[type="email"]', adminEmail)
    await page.click('button[type="submit"]')

    // Xác nhận hiển thị thông điệp trung lập
    const successMsg = page.locator('p.success-message')
    await expect(successMsg).toBeVisible()
    await expect(successMsg).toContainText('hướng dẫn đặt lại mật khẩu đã được gửi')

    // 6. Lấy email và reset link từ Mailpit
    const { token: resetToken } = await fetchLatestResetToken(adminEmail)
    expect(resetToken.length).toBeGreaterThanOrEqual(32)

    const browserResetUrl = `${origin}/reset-password?token=${resetToken}`

    // 7. Mở link bằng trình duyệt thật
    await page.goto(browserResetUrl)

    // Xác nhận bảo mật token trong URL: Sau khi nạp trang, URL không còn chứa query token
    await expect(page).toHaveURL(`${origin}/reset-password`)
    expect(page.url()).not.toContain('token=')

    // Xác nhận ô token trong form vẫn chứa giá trị token hợp lệ
    const tokenInput = page.locator('input[placeholder="Nhập mã token từ liên kết email"]')
    await expect(tokenInput).toHaveValue(resetToken)

    // Theo dõi request của trình duyệt và xác nhận raw token không xuất hiện trong header Referer của request tiếp theo
    let totalSubsequentRequests = 0
    const requestListener = (req: Request) => {
      totalSubsequentRequests++
      const ref = req.headers()['referer']
      if (ref) {
        expect(ref).not.toContain(resetToken)
      }
    }
    page.on('request', requestListener)

    // 8. Đặt mật khẩu mới trên form giao diện
    const newPasswordInput = page.locator('input[placeholder="Tối thiểu 8 ký tự"]')
    const confirmPasswordInput = page.locator('input[placeholder="Nhập lại mật khẩu mới"]')

    await newPasswordInput.fill(targetNewPass)
    await confirmPasswordInput.fill(targetNewPass)
    await page.click('button[type="submit"]')

    // Xác nhận thông báo đổi mật khẩu thành công trên giao diện
    const resetSuccessBox = page.locator('div.success-box')
    await expect(resetSuccessBox).toBeVisible()
    await expect(resetSuccessBox.locator('p.success-message')).toContainText(
      'Đặt lại mật khẩu thành công',
    )

    // Xác nhận đã có request đi và Referer không chứa raw token
    expect(totalSubsequentRequests).toBeGreaterThan(0)
    page.off('request', requestListener)

    // 9. Đăng nhập thành công bằng mật khẩu mới
    await page.click('a:has-text("Đăng nhập ngay")')
    await expect(page).toHaveURL(`${origin}/login`)

    await page.fill('input[type="email"]', adminEmail)
    await page.fill('input[type="password"]', targetNewPass)
    await page.click('button[type="submit"]')

    await expect(page).toHaveURL(`${origin}/`)
    await expect(page.locator('header.topbar strong')).toContainText('Hệ thống Quản lý Thiết bị')

    // 10. Đăng xuất và xác nhận mật khẩu cũ không còn đăng nhập được
    await page.click('button:has-text("Đăng xuất")')
    await expect(page).toHaveURL(`${origin}/login`)

    await page.fill('input[type="email"]', adminEmail)
    await page.fill('input[type="password"]', oldPassBeforeReset)
    await page.click('button[type="submit"]')

    await expect(page).toHaveURL(`${origin}/login`)
    const loginError = page.locator('p.error-message')
    await expect(loginError).toBeVisible()
    await expect(loginError).toContainText('Chưa xác thực')

    // 11. Đảm bảo dữ liệu chạy lại được (Self-healing restore về initialPass nếu đang ở tempNewPass)
    if (targetNewPass !== initialPass) {
      await ensureCooldownExpired()
      await clearMailpitMessages()

      await page.click('a:has-text("Quên mật khẩu?")')
      await expect(page).toHaveURL(`${origin}/forgot-password`)
      await page.fill('input[type="email"]', adminEmail)
      await page.click('button[type="submit"]')
      await expect(page.locator('p.success-message')).toBeVisible()

      const { token: restoreToken } = await fetchLatestResetToken(adminEmail)
      await page.goto(`${origin}/reset-password?token=${restoreToken}`)
      await expect(page).toHaveURL(`${origin}/reset-password`)

      await page.locator('input[placeholder="Tối thiểu 8 ký tự"]').fill(initialPass)
      await page.locator('input[placeholder="Nhập lại mật khẩu mới"]').fill(initialPass)
      await page.click('button[type="submit"]')
      await expect(page.locator('div.success-box')).toBeVisible()

      // Xác nhận mật khẩu ban đầu hoạt động trở lại
      await page.goto(`${origin}/login`)
      await page.fill('input[type="email"]', adminEmail)
      await page.fill('input[type="password"]', initialPass)
      await page.click('button[type="submit"]')
      await expect(page).toHaveURL(`${origin}/`)
      await page.click('button:has-text("Đăng xuất")')
    }
  })
})
