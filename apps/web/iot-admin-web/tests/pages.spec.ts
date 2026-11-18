import { expect, test } from '@playwright/test'

test('首页可以导航到登录页', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: '物联网管理平台' })).toBeVisible()
  await page.getByRole('link', { name: '前往登录' }).click()
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: '登录账号' })).toBeVisible()
})

test('首页按钮请求后端测试接口并记录请求与响应', async ({ page }) => {
  const logs: string[] = []
  page.on('console', (entry) => logs.push(entry.text()))
  await page.route('**/api/admin/test/auth', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: '{"code":200,"traceId":"test-trace","msg":"OK","data":null}',
    }),
  )

  await page.goto('/')
  const requestPromise = page.waitForRequest('**/api/admin/test/auth')
  await page.getByRole('button', { name: '测试后端接口' }).click()
  const request = await requestPromise

  expect(request.method()).toBe('GET')
  expect(new URL(request.url()).origin).toBe(new URL(page.url()).origin)
  await expect(page.getByText('接口请求成功')).toBeVisible()
  expect(logs.some((log) => log.includes('测试接口请求') && log.includes('GET'))).toBe(true)
  expect(logs.some((log) => log.includes('测试接口响应') && log.includes('200'))).toBe(true)
})

test('业务码 201 也视为成功', async ({ page }) => {
  await page.route('**/api/admin/test/auth', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: '{"code":201,"traceId":"test-trace","msg":"Created","data":null}',
    }),
  )

  await page.goto('/')
  await page.getByRole('button', { name: '测试后端接口' }).click()
  await expect(page.getByText('接口请求成功')).toBeVisible()
  await expect(page.getByRole('alert')).not.toBeVisible()
})

const unauthorizedBody = JSON.stringify({
  code: 401,
  traceId: '0f8ecebbb9a1408bbb829b588b8858ec',
  msg: 'Unauthorized',
  data: null,
})

for (const responseCase of [
  { name: 'HTTP 401', status: 401 },
  { name: '业务码 401', status: 200 },
]) {
  test(`${responseCase.name} 会跳转登录页`, async ({ page }) => {
    await page.route('**/api/admin/test/auth', (route) =>
      route.fulfill({
        status: responseCase.status,
        contentType: 'application/json',
        body: unauthorizedBody,
      }),
    )

    await page.goto('/')
    await page.evaluate(() => localStorage.setItem('iot-admin-token', 'expired-token'))
    await page.getByRole('button', { name: '测试后端接口' }).click()
    await expect(page).toHaveURL(/\/login$/)
    expect(await page.evaluate(() => localStorage.getItem('iot-admin-token'))).toBeNull()
    await expect(page.getByRole('heading', { name: '登录账号' })).toBeVisible()
    await expect(page.getByText('接口请求成功')).not.toBeVisible()
    const alert = page.getByRole('alert')
    await expect(alert).toHaveText('Unauthorized')
    await expect(alert).not.toBeVisible({ timeout: 4000 })
  })
}

test('非成功业务码展示 msg 并在 2 秒后消失', async ({ page }) => {
  await page.route('**/api/admin/test/auth', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: '{"code":400,"traceId":"test-trace","msg":"请求参数错误","data":null}',
    }),
  )

  await page.goto('/')
  await page.getByRole('button', { name: '测试后端接口' }).click()
  const alert = page.getByRole('alert')
  await expect(alert).toHaveText('请求参数错误')
  const offset = await alert.evaluate((element) => {
    const box = element.getBoundingClientRect()
    return Math.abs(box.x + box.width / 2 - window.innerWidth / 2)
  })
  expect(offset).toBeLessThan(2)
  await expect(alert).not.toBeVisible({ timeout: 4000 })
  await expect(page).toHaveURL(/\/$/)
})

test('登录保存 token 并为后续请求添加 Bearer 头', async ({ page }) => {
  await page.route('**/api/admin/user/login', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        traceId: 'login-trace',
        msg: '',
        data: { id: 1, username: 'admin', token: 'test-access-token', refreshToken: 'test-refresh-token' },
      }),
    }),
  )
  await page.route('**/api/admin/test/auth', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: '{"code":200,"traceId":"test-trace","msg":"","data":null}',
    }),
  )

  await page.goto('/login')
  await page.evaluate(() => localStorage.setItem('iot-admin-token', 'old-token'))
  const loginRequestPromise = page.waitForRequest('**/api/admin/user/login')
  await page.getByRole('textbox', { name: '用户名' }).fill('admin')
  await page.getByLabel('密码', { exact: true }).fill('password')
  await page.locator('button[type="submit"]').click()
  const loginRequest = await loginRequestPromise
  expect(loginRequest.method()).toBe('POST')
  expect(loginRequest.postDataJSON()).toEqual({ username: 'admin', password: 'password' })
  expect(loginRequest.headers().authorization).toBeUndefined()
  await expect(page).toHaveURL(/\/$/)
  expect(await page.evaluate(() => localStorage.getItem('iot-admin-token'))).toBe('test-access-token')

  await page.reload()
  const authRequestPromise = page.waitForRequest('**/api/admin/test/auth')
  await page.getByRole('button', { name: '测试后端接口' }).click()
  const authRequest = await authRequestPromise
  expect(authRequest.headers().authorization).toBe('Bearer test-access-token')
  await expect(page.getByText('接口请求成功')).toBeVisible()
})

test('登录失败时停留在登录页并显示服务端消息', async ({ page }) => {
  await page.route('**/api/admin/user/login', (route) =>
    route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: unauthorizedBody,
    }),
  )

  await page.goto('/login')
  await page.getByRole('textbox', { name: '用户名' }).fill('admin')
  await page.getByLabel('密码', { exact: true }).fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('alert')).toHaveText('Unauthorized')
  await expect(page).toHaveURL(/\/login$/)
  expect(await page.evaluate(() => localStorage.getItem('iot-admin-token'))).toBeNull()
})
