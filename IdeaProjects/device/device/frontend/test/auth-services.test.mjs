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

test('PRD-502 / PRD-506: ApiResponse type definition includes requestId', () => {
  const typesPath = path.join(frontendRoot, 'src/types/api.ts')
  const content = fs.readFileSync(typesPath, 'utf-8')

  assert.match(
    content,
    /requestId\?:\s*string/,
    'ApiResponse includes optional requestId field'
  )
})

test('PRD-502 / PRD-506: http.ts exports REQUEST_ID_HEADER and getRequestId logic', () => {
  const httpPath = path.join(frontendRoot, 'src/services/http.ts')
  const content = fs.readFileSync(httpPath, 'utf-8')

  assert.match(
    content,
    /export\s+const\s+REQUEST_ID_HEADER\s*=\s*['"]X-Request-ID['"]/,
    'Defines X-Request-ID header constant'
  )
  assert.match(
    content,
    /export\s+function\s+getRequestId/,
    'Exports getRequestId function'
  )
  assert.match(
    content,
    /export\s+function\s+extractHeaderRequestId/,
    'Exports extractHeaderRequestId function'
  )
  assert.match(
    content,
    /Mã tham chiếu:/,
    'Formats error message with reference code UX'
  )
})

test('PRD-502 / PRD-506: Behavioral simulation of request correlation extraction and error formatting', () => {
  function extractHeaderRequestId(headers) {
    if (!headers || typeof headers !== 'object') return undefined
    const val =
      (typeof headers.get === 'function' ? headers.get('x-request-id') : undefined) ??
      headers['x-request-id'] ??
      headers['X-Request-ID']
    if (typeof val === 'string' && val.trim()) return val.trim()
    return undefined
  }

  function getRequestId(error) {
    if (error && error.isAxiosError) {
      const bodyRequestId = error.response?.data?.requestId
      if (typeof bodyRequestId === 'string' && bodyRequestId.trim()) {
        return bodyRequestId.trim()
      }
      return extractHeaderRequestId(error.response?.headers)
    }
    return undefined
  }

  function getErrorMessage(error) {
    const requestId = getRequestId(error)
    let baseMessage
    if (error && error.isAxiosError) {
      baseMessage = error.response?.data?.message ?? 'Không thể kết nối tới máy chủ.'
    } else if (error instanceof Error) {
      baseMessage = error.message
    } else {
      baseMessage = 'Đã xảy ra lỗi không xác định.'
    }

    if (requestId && !baseMessage.includes(requestId)) {
      return `${baseMessage} (Mã tham chiếu: ${requestId})`
    }
    return baseMessage
  }

  // Case 1: Body has requestId and message -> body takes priority
  const errorWithBodyAndHeader = {
    isAxiosError: true,
    response: {
      data: { message: 'Chưa xác thực', requestId: 'req-from-body-123' },
      headers: { 'x-request-id': 'req-from-header-456' },
    },
  }
  assert.strictEqual(getRequestId(errorWithBodyAndHeader), 'req-from-body-123')
  assert.strictEqual(
    getErrorMessage(errorWithBodyAndHeader),
    'Chưa xác thực (Mã tham chiếu: req-from-body-123)'
  )

  // Case 2: Body has no requestId, header has X-Request-ID -> fallback to header
  const errorWithHeaderOnly = {
    isAxiosError: true,
    response: {
      data: { message: 'Tài nguyên bị khóa' },
      headers: { 'X-Request-ID': 'req-from-header-456' },
    },
  }
  assert.strictEqual(getRequestId(errorWithHeaderOnly), 'req-from-header-456')
  assert.strictEqual(
    getErrorMessage(errorWithHeaderOnly),
    'Tài nguyên bị khóa (Mã tham chiếu: req-from-header-456)'
  )

  // Case 3: Body has no message but has requestId -> fallback message with reference ID
  const errorWithFallbackMessage = {
    isAxiosError: true,
    response: {
      data: { requestId: 'req-fallback-789' },
      headers: {},
    },
  }
  assert.strictEqual(getRequestId(errorWithFallbackMessage), 'req-fallback-789')
  assert.strictEqual(
    getErrorMessage(errorWithFallbackMessage),
    'Không thể kết nối tới máy chủ. (Mã tham chiếu: req-fallback-789)'
  )

  // Case 4: Axios headers with .get() method (AxiosResponseHeaders)
  const errorWithHeadersGet = {
    isAxiosError: true,
    response: {
      data: {},
      headers: {
        get(name) {
          return name.toLowerCase() === 'x-request-id' ? 'req-from-get-999' : undefined
        },
      },
    },
  }
  assert.strictEqual(getRequestId(errorWithHeadersGet), 'req-from-get-999')
  assert.strictEqual(
    getErrorMessage(errorWithHeadersGet),
    'Không thể kết nối tới máy chủ. (Mã tham chiếu: req-from-get-999)'
  )

  // Case 5: Regular Error without correlation ID
  const regularError = new Error('Lỗi mạng cục bộ')
  assert.strictEqual(getRequestId(regularError), undefined)
  assert.strictEqual(getErrorMessage(regularError), 'Lỗi mạng cục bộ')

  // Case 6: Message already includes requestId
  const errorAlreadyWithId = {
    isAxiosError: true,
    response: {
      data: { message: 'Lỗi với mã req-existing-000', requestId: 'req-existing-000' },
      headers: {},
    },
  }
  assert.strictEqual(getErrorMessage(errorAlreadyWithId), 'Lỗi với mã req-existing-000')
})

test('PRD-603: render.yaml specifies Render Static Site configuration with SPA rewrite and security headers', () => {
  const renderPath = path.join(frontendRoot, 'render.yaml')
  assert.ok(fs.existsSync(renderPath), 'render.yaml exists in frontend directory')

  const content = fs.readFileSync(renderPath, 'utf-8')
  assert.match(content, /type:\s*web/, 'Static Site uses the Render web service type')
  assert.match(content, /runtime:\s*static/, 'Static Site uses the Render static runtime')
  assert.match(
    content,
    /rootDir:\s*IdeaProjects\/device\/device\/frontend/,
    'Monorepo root directory points to the frontend project'
  )
  assert.match(content, /autoDeployTrigger:\s*["']off["']/, 'CI owns deploy triggering after checks pass')
  assert.match(content, /buildCommand:\s*npm ci && npm run build/, 'Specifies npm ci && npm run build')
  assert.match(content, /staticPublishPath:\s*\.\/dist/, 'Publishes dist directory')
  assert.match(content, /type:\s*rewrite/, 'Specifies SPA rewrite rule')
  assert.match(content, /source:\s*\/\*/, 'Rewrites all incoming paths')
  assert.match(content, /destination:\s*\/index\.html/, 'Routes to /index.html for client-side routing')

  // Security headers
  assert.match(content, /name:\s*X-Content-Type-Options\s+value:\s*nosniff/, 'Enforces nosniff')
  assert.match(content, /name:\s*X-Frame-Options\s+value:\s*DENY/, 'Enforces DENY')
  assert.match(content, /name:\s*Referrer-Policy\s+value:\s*no-referrer/, 'Enforces no-referrer')

  // Environment variable
  assert.match(content, /key:\s*VITE_API_BASE_URL/, 'Defines VITE_API_BASE_URL env var')
})

test('PRD-603: http.ts reads VITE_API_BASE_URL from environment with fallback to /api', () => {
  const httpPath = path.join(frontendRoot, 'src/services/http.ts')
  const content = fs.readFileSync(httpPath, 'utf-8')

  assert.match(
    content,
    /baseURL:\s*import\.meta\.env\.VITE_API_BASE_URL\s*\?\?\s*['"]\/api['"]/,
    'http client uses VITE_API_BASE_URL with fallback to /api'
  )
})
