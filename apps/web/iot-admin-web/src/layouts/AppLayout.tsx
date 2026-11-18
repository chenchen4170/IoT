import { Outlet } from 'react-router'
import { SiteFooter } from '../components/SiteFooter'
import { SiteHeader } from '../components/SiteHeader'

export function AppLayout() {
  return (
    <div className="app-shell">
      <SiteHeader />
      <main className="page-content"><Outlet /></main>
      <SiteFooter />
    </div>
  )
}
