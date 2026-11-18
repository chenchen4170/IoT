import { createBrowserRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { ApiErrorAlert } from './components/ApiErrorAlert'
import { AppLayout } from './layouts/AppLayout'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { setUnauthorizedHandler } from './services/http'
import './App.css'

const router = createBrowserRouter([
  {
    path: '/',
    Component: AppLayout,
    children: [
      { index: true, Component: HomePage },
      { path: 'login', Component: LoginPage },
    ],
  },
])

setUnauthorizedHandler(() => {
  if (router.state.location.pathname !== '/login') {
    void router.navigate('/login', { replace: true })
  }
})

export default function App() {
  return (
    <>
      <RouterProvider router={router} />
      <ApiErrorAlert />
    </>
  )
}
