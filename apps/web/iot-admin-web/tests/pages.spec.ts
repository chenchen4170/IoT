import { expect, test } from '@playwright/test'

test('首页可以导航到登录页', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: '物联网管理平台' })).toBeVisible()
  await page.getByRole('link', { name: '前往登录' }).click()
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: '登录账号' })).toBeVisible()
})

test('登录页可以直接访问并提示未接入服务', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('textbox', { name: '用户名' }).fill('admin')
  await page.getByLabel('密码', { exact: true }).fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByText('登录服务尚未接入', { exact: true })).toBeVisible()
  await expect(page).toHaveURL(/\/login$/)
})
