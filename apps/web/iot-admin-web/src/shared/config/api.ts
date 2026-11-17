// During development, requests go through Vite's /api proxy.
export const apiBaseUrl = import.meta.env.DEV
  ? ''
  : (import.meta.env.VITE_API_BASE_URL ?? '').trim().replace(/\/+$/, '')
