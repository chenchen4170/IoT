import { createBrowserRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { AppLayout } from './layouts/AppLayout'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
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

export default function App() {
  return <RouterProvider router={router} />
}
