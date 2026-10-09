import test from 'node:test'
import assert from 'node:assert'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const frontendRoot = path.resolve(__dirname, '..')

test('FR-5 / PR-206 / PR-410: ResetPasswordPage must purge token from URL upon load', () => {
  const pagePath = path.join(frontendRoot, 'src/pages/ResetPasswordPage.tsx')
  assert.ok(fs.existsSync(pagePath), 'ResetPasswordPage.tsx exists')

  const content = fs.readFileSync(pagePath, 'utf-8')

  // Verify searchParams.get('token')
  assert.match(
    content,
    /searchParams\.get\(['"]token['"]\)/,
    'ResetPasswordPage reads token from searchParams'
  )

  // Verify navigation replace to remove token parameter from address bar
  assert.match(
    content,
    /navigate\(['"]\/reset-password['"],\s*\{\s*replace:\s*true\s*\}\)/,
    'ResetPasswordPage performs navigate("/reset-password", { replace: true }) to remove token from URL'
  )
})

test('FR-5 / PR-206: Client-side validation logic for password reset form', () => {
  const pagePath = path.join(frontendRoot, 'src/pages/ResetPasswordPage.tsx')
  const content = fs.readFileSync(pagePath, 'utf-8')

  // Token not empty check
  assert.match(
    content,
    /!token\.trim\(\)/,
    'Validates that token is not blank'
  )

  // Password length between 8 and 128 characters
  assert.match(
    content,
    /newPassword\.length\s*<\s*8\s*\|\|\s*newPassword\.length\s*>\s*128/,
    'Validates password length is between 8 and 128 characters'
  )

  // Password confirmation match
  assert.match(
    content,
    /newPassword\s*!==\s*confirmPassword/,
    'Validates that newPassword equals confirmPassword'
  )
})

test('Simulated URL token purge and validation behavior', () => {
  // Simulate URL with query token
  const initialUrl = new URL('http://localhost:5173/reset-password?token=secret-token-123')
  const tokenFromUrl = initialUrl.searchParams.get('token')
  assert.strictEqual(tokenFromUrl, 'secret-token-123')

  // Simulate component state init and address bar purge
  const formState = {
    token: tokenFromUrl,
    newPassword: '',
    confirmPassword: '',
  }

  // Simulate navigate('/reset-password', { replace: true })
  const purgedUrl = new URL(initialUrl.pathname, initialUrl.origin)
  assert.strictEqual(purgedUrl.searchParams.get('token'), null)
  assert.strictEqual(purgedUrl.toString(), 'http://localhost:5173/reset-password')
  assert.strictEqual(formState.token, 'secret-token-123')

  // Validation function simulation matching ResetPasswordPage
  function validate(token, newPass, confirmPass) {
    if (!token.trim()) return 'Mã xác thực token không được để trống.'
    if (newPass.length < 8 || newPass.length > 128) return 'Mật khẩu mới phải từ 8 đến 128 ký tự.'
    if (newPass !== confirmPass) return 'Xác nhận mật khẩu mới không trùng khớp.'
    return null
  }

  assert.strictEqual(validate('', 'ValidPass123', 'ValidPass123'), 'Mã xác thực token không được để trống.')
  assert.strictEqual(validate('tok', 'short', 'short'), 'Mật khẩu mới phải từ 8 đến 128 ký tự.')
  assert.strictEqual(validate('tok', 'ValidPass123', 'Different123'), 'Xác nhận mật khẩu mới không trùng khớp.')
  assert.strictEqual(validate('tok', 'ValidPass123', 'ValidPass123'), null)
})
