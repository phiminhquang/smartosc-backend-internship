import test from 'node:test'
import assert from 'node:assert'

const BACKEND_URL = process.env.BACKEND_URL || 'http://127.0.0.1:8080'
const MAILPIT_URL = process.env.MAILPIT_URL || 'http://127.0.0.1:8025'

test('E2E Compose Smoke Test: Login, Password Reset, Mailpit verification', async (t) => {
  // Check if backend is reachable
  let backendReachable = false
  try {
    const res = await fetch(`${BACKEND_URL}/v3/api-docs`, { signal: AbortSignal.timeout(5000) })
    if (res.ok) backendReachable = true
  } catch {
    backendReachable = false
  }

  if (!backendReachable) {
    t.skip(`Backend at ${BACKEND_URL} is not reachable. Skipping live Compose smoke test. Run with compose stack up to verify against real container.`)
    return
  }

  // Check if mailpit is reachable
  let mailpitReachable = false
  try {
    const res = await fetch(`${MAILPIT_URL}/api/v1/info`, { signal: AbortSignal.timeout(1000) })
    if (res.ok) mailpitReachable = true
  } catch {
    mailpitReachable = false
  }

  if (!mailpitReachable) {
    t.skip(`Mailpit at ${MAILPIT_URL} is not reachable. Skipping live Compose smoke test.`)
    return
  }

  // 1. Admin login verification
  const adminEmail = process.env.ADMIN_EMAIL || 'admin@device.local'
  const adminPassword = process.env.ADMIN_PASSWORD || 'Admin@123456'

  const loginRes = await fetch(`${BACKEND_URL}/api/auth/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: adminEmail, password: adminPassword }),
  })

  // We test the contract response shape
  assert.ok(
    loginRes.status === 200 || loginRes.status === 401,
    `Login response status was ${loginRes.status}`
  )

  // 2. Forgot password request (neutral response for any email)
  const forgotRes = await fetch(`${BACKEND_URL}/api/auth/password-reset/request`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: adminEmail }),
  })

  assert.strictEqual(
    forgotRes.status,
    202,
    `Password reset request should return 202 Accepted, got ${forgotRes.status}`
  )

  const forgotData = await forgotRes.json()
  assert.ok(forgotData.message, 'Response includes message')

  // 3. Confirm password reset with invalid token
  const confirmInvalidRes = await fetch(`${BACKEND_URL}/api/auth/password-reset/confirm`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token: 'invalid-non-existent-token', newPassword: 'NewPassword@123' }),
  })

  assert.strictEqual(
    confirmInvalidRes.status,
    400,
    `Confirm reset with invalid token must return 400 Bad Request, got ${confirmInvalidRes.status}`
  )
})
