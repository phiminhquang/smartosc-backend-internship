import test from 'node:test'
import assert from 'node:assert'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const frontendRoot = path.resolve(__dirname, '..')

test('FR-5 / PR-206 / PR-410: index.html must specify no-referrer policy', () => {
  const indexPath = path.join(frontendRoot, 'index.html')
  assert.ok(fs.existsSync(indexPath), 'index.html exists')

  const content = fs.readFileSync(indexPath, 'utf-8')
  assert.match(
    content,
    /<meta\s+name=["']referrer["']\s+content=["']no-referrer["']\s*\/?>/i,
    'index.html contains meta tag with referrer no-referrer policy'
  )
})

test('PRD-103: nginx.conf must enforce security headers and Referrer-Policy', () => {
  const nginxPath = path.join(frontendRoot, 'nginx.conf')
  assert.ok(fs.existsSync(nginxPath), 'nginx.conf exists')

  const content = fs.readFileSync(nginxPath, 'utf-8')
  assert.match(
    content,
    /add_header\s+Referrer-Policy\s+["']no-referrer["']\s+always;/i,
    'nginx.conf sets Referrer-Policy no-referrer'
  )
  assert.match(
    content,
    /add_header\s+X-Content-Type-Options\s+["']nosniff["']\s+always;/i,
    'nginx.conf sets X-Content-Type-Options nosniff'
  )
  assert.match(
    content,
    /add_header\s+X-Frame-Options\s+["']DENY["']\s+always;/i,
    'nginx.conf sets X-Frame-Options DENY'
  )
})

test('Security check: source code must not contain hardcoded raw reset tokens or test credentials', () => {
  const srcDir = path.join(frontendRoot, 'src')
  const files = []

  function scan(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const fullPath = path.join(dir, entry.name)
      if (entry.isDirectory()) {
        scan(fullPath)
      } else if (/\.(tsx?|jsx?|html)$/.test(entry.name)) {
        files.push(fullPath)
      }
    }
  }

  scan(srcDir)
  assert.ok(files.length > 0, 'Found source files to scan')

  for (const file of files) {
    const content = fs.readFileSync(file, 'utf-8')
    assert.doesNotMatch(
      content,
      /eyJ[a-zA-Z0-9_-]{20,}/,
      `File ${path.relative(frontendRoot, file)} should not contain hardcoded JWT tokens`
    )
  }
})
