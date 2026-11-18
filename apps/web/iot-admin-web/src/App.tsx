import { Button, Form, Input, message } from 'antd'
import { Link, NavLink, Outlet } from 'react-router'
import './App.css'

export function AppLayout() {
  return (
    <div className="app-shell">
      <header className="site-header">
        <Link className="brand" to="/" aria-label="物联网管理平台首页">
          <span className="brand-mark" aria-hidden="true"><span /><span /><span /><span /></span>
          <span>物联网管理平台</span>
        </Link>
        <nav className="site-nav" aria-label="主导航">
          <NavLink to="/" end>首页</NavLink>
          <NavLink to="/login">登录</NavLink>
        </nav>
      </header>
      <main className="page-content"><Outlet /></main>
      <footer className="site-footer">物联网管理平台</footer>
    </div>
  )
}

export function HomePage() {
  return (
    <div className="home-page">
      <section className="welcome">
        <p className="eyebrow">控制台 / 首页</p>
        <h1>物联网管理平台</h1>
        <p className="lead">设备、数据与日常运营的统一入口。</p>
        <Link className="primary-link" to="/login">前往登录 <span aria-hidden="true">→</span></Link>
      </section>
      <section className="overview" aria-labelledby="overview-title">
        <div className="section-heading">
          <h2 id="overview-title">平台概览</h2>
          <span>基础信息</span>
        </div>
        <div className="overview-list">
          <div><span className="overview-index">01</span><h3>设备管理</h3><p>集中查看与管理接入设备。</p></div>
          <div><span className="overview-index">02</span><h3>数据监测</h3><p>了解设备数据与运行状态。</p></div>
          <div><span className="overview-index">03</span><h3>运营维护</h3><p>为日常管理提供统一工作入口。</p></div>
        </div>
      </section>
    </div>
  )
}

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
