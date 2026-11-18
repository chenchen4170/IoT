import { useState } from 'react'
import { Button, Form, Input, message } from 'antd'
import { useNavigate } from 'react-router'
import { login } from '../services/api/login'
import type { LoginRequest } from '../services/api/login'
import { saveToken } from '../services/auth'
import { isHandledApiError } from '../services/http'

export function LoginPage() {
  const navigate = useNavigate()
  const [isSubmitting, setIsSubmitting] = useState(false)

  const onFinish = async (values: LoginRequest) => {
    setIsSubmitting(true)
    try {
      const { token } = await login(values)
      saveToken(token)
      void navigate('/', { replace: true })
    } catch (error) {
      if (isHandledApiError(error)) return
      message.error(error instanceof Error ? error.message : '登录失败，请稍后重试')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-intro">
        <p className="eyebrow">账户登录</p>
        <h1>欢迎回来</h1>
        <p>登录物联网管理平台，开始管理设备与数据。</p>
      </div>
      <div className="login-panel">
        <h2>登录账号</h2>
        <p className="form-hint">请输入您的账号信息</p>
        <Form<LoginRequest> layout="vertical" onFinish={onFinish} requiredMark={false} disabled={isSubmitting}>
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="请输入用户名" autoComplete="username" size="large" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" autoComplete="current-password" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" size="large" loading={isSubmitting} block>登录</Button>
        </Form>
      </div>
    </div>
  )
}
