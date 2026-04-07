import { http } from './http'
import type { ApiResponse, Product } from './types'

export async function listProducts(): Promise<Product[]> {
  const { data } = await http.get<ApiResponse<Product[]>>('/api/products')
  return data.data ?? []
}

export async function getProduct(id: number): Promise<Product | null> {
  const { data } = await http.get<ApiResponse<Product>>(`/api/products/${id}`)
  if (data.code !== 0) return null
  return data.data ?? null
}

export async function createProduct(input: {
  name: string
  price: number
  stock: number
  description?: string
}): Promise<Product> {
  // 注意：后端的 POST 建议走 /api/products/（末尾 /）避免 301 导致 POST 变 GET
  const { data } = await http.post<ApiResponse<Product>>('/api/products/', input)
  if (data.code !== 0 || !data.data) throw new Error(data.message || 'create product failed')
  return data.data
}

export async function searchProducts(keyword: string): Promise<Product[]> {
  const { data } = await http.get<ApiResponse<Product[]>>('/api/products/search', { params: { keyword } })
  return data.data ?? []
}

