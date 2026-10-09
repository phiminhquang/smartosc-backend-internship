import test from 'node:test'
import assert from 'node:assert'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const frontendRoot = path.resolve(__dirname, '..')

test('Auth contract: TOKEN_STORAGE_KEY must be "device_access_token"', () => {
  const httpPath = path.join(frontendRoot, 'src/services/http.ts')
  const content = fs.readFileSync(httpPath, 'utf-8')
  assert.match(
    content,
    /export\s+const\s+TOKEN_STORAGE_KEY\s*=\s*['"]device_access_token['"]/,
    'TOKEN_STORAGE_KEY is properly defined'
  )
})

test('Auth contract: http interceptor injects Authorization Bearer token from sessionStorage', () => {
  const httpPath = path.join(frontendRoot, 'src/services/http.ts')
  const content = fs.readFileSync(httpPath, 'utf-8')

  assert.match(
    content,
    /sessionStorage\.getItem\(TOKEN_STORAGE_KEY\)/,
    'Retrieves token from sessionStorage'
  )
  assert.match(
    content,
    /config\.headers\.Authorization\s*=\s*`Bearer \$\{token\}`/,
    'Sets Bearer Authorization header'
  )
})

test('Auth contract: authService endpoints and payload contracts', () => {
  const authServicePath = path.join(frontendRoot, 'src/services/authService.ts')
  const content = fs.readFileSync(authServicePath, 'utf-8')

  // /auth/token
  assert.match(content, /['"]\/auth\/token['"]/, 'Uses /auth/token for login')
  // /auth/password-reset/request
  assert.match(
    content,
    /['"]\/auth\/password-reset\/request['"]/,
    'Uses /auth/password-reset/request for requesting reset'
  )
  // /auth/password-reset/confirm
  assert.match(
    content,
    /['"]\/auth\/password-reset\/confirm['"]/,
    'Uses /auth/password-reset/confirm for confirming reset'
  )

  // Neutral message fallback for forgot password
  assert.match(
    content,
    /Nếu email tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi/,
    'Fallback neutral message is present'
  )
})

test('Error parsing: getErrorMessage extracts backend ApiResponse message or fallback', () => {
  const httpPath = path.join(frontendRoot, 'src/services/http.ts')
  const content = fs.readFileSync(httpPath, 'utf-8')

  assert.match(
    content,
    /error\.response\?\.data\?\.message\s*\?\?\s*['"]Không thể kết nối tới máy chủ\.['"]/,
    'Extracts server error message with connection fallback'
  )
})
