import { Link, NavLink } from 'react-router'

export function SiteHeader() {
  return (
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
  )
}
