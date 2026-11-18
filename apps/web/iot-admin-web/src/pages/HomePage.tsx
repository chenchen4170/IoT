import { useState } from 'react'
import { Button, message } from 'antd'
import { Link } from 'react-router'
import { getTestAuth } from '../services/api/testAuth'
import { isHandledApiError } from '../services/http'

export function HomePage() {
  const [isTesting, setIsTesting] = useState(false)

  const testAuth = async () => {
    setIsTesting(true)

    try {
      await getTestAuth()
      message.success('接口请求成功')
    } catch (error) {
      if (isHandledApiError(error)) return
      console.error('测试接口请求失败', error)
      message.error('接口请求失败，请查看控制台日志')
    } finally {
      setIsTesting(false)
    }
  }

  return (
    <div className="home-page">
      <section className="welcome">
        <p className="eyebrow">控制台 / 首页</p>
        <h1>物联网管理平台</h1>
        <p className="lead">设备、数据与日常运营的统一入口。</p>
        <div className="welcome-actions">
          <Link className="primary-link" to="/login">前往登录 <span aria-hidden="true">→</span></Link>
          <Button loading={isTesting} onClick={testAuth}>测试后端接口</Button>
        </div>
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
