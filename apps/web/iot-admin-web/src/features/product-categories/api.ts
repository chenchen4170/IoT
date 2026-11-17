import { getJson } from '../../shared/api/client'
import type { ProductCategory } from './types'

export async function getProductCategories(signal?: AbortSignal) {
  const categories = await getJson<ProductCategory[]>('/api/admin/product-categories', signal)

  if (!Array.isArray(categories)) {
    throw new Error('产品分类接口返回了无效数据')
  }

  return categories
}
