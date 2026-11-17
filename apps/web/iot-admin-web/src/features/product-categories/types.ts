export interface ProductCategory {
  id: number
  parentId: number | null
  categoryCode: string
  categoryName: string
  description: string | null
  iconUrl: string | null
  sortOrder: number
  status: number
  createdAt: string | null
  updatedAt: string | null
}
