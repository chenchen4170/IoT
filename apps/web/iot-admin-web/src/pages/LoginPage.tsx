import { Button, Form, Input, message } from 'antd'

type LoginValues = {
  username: string
  password: string
}

export function LoginPage() {
  const onFinish = () => {
    message.info('登录服务尚未接入')
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
        <Form<LoginValues> layout="vertical" onFinish={onFinish} requiredMark={false}>
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="请输入用户名" autoComplete="username" size="large" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" autoComplete="current-password" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" size="large" block>登录</Button>
        </Form>
        <p className="login-note">当前仅提供页面展示，登录服务尚未接入。</p>
      </div>
    </div>
  )
}
