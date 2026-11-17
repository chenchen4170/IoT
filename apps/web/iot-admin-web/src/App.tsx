import { useEffect, useState } from 'react'
import { getProductCategories } from './features/product-categories/api'
import type { ProductCategory } from './features/product-categories/types'
import './App.css'

function App() {
  const [categories, setCategories] = useState<ProductCategory[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [requestId, setRequestId] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    getProductCategories(controller.signal)
      .then((data) => {
        setCategories(data)
        setError('')
      })
      .catch((reason: unknown) => {
        if (!controller.signal.aborted) {
          setError(reason instanceof Error ? reason.message : '获取产品分类失败')
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })

    return () => controller.abort()
  }, [requestId])

  function refresh() {
    setError('')
    setLoading(true)
    setRequestId((current) => current + 1)
  }

  return (
    <main className="admin-page">
      <header className="page-header">
        <div>
          <p className="section-label">产品管理</p>
          <h1>产品分类</h1>
        </div>
        <button type="button" onClick={refresh} disabled={loading}>
          刷新
        </button>
      </header>

      <section className="category-section" aria-label="产品分类列表">
        {error && (
          <div className="request-error" role="alert">
            <span>{error}</span>
            <button type="button" onClick={refresh}>重试</button>
          </div>
        )}
        {loading ? (
          <p className="table-message" role="status">正在加载产品分类…</p>
        ) : !error && categories.length === 0 ? (
          <p className="table-message">暂无产品分类</p>
        ) : !error ? (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th scope="col">分类名称</th>
                  <th scope="col">分类编码</th>
                  <th scope="col">上级 ID</th>
                  <th scope="col">排序</th>
                  <th scope="col">状态</th>
                </tr>
              </thead>
              <tbody>
                {categories.map((category) => (
                  <tr key={category.id}>
                    <td>{category.categoryName}</td>
                    <td className="muted">{category.categoryCode}</td>
                    <td className="muted">{category.parentId ?? '—'}</td>
                    <td>{category.sortOrder}</td>
                    <td>
                      <span className={category.status === 1 ? 'status active' : 'status'}>
                        {category.status === 1 ? '启用' : '停用'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </main>
  )
}

export default App
