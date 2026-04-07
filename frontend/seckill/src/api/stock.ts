import { http } from './http'
import type { ApiResponse, Stock } from './types'

export async function createStock(input: { productId: number; quantity: number }): Promise<Stock> {
  const { data } = await http.post<ApiResponse<Stock>>('/api/stocks', input)
  if (data.code !== 0 || !data.data) throw new Error(data.message || 'create stock failed')
  return data.data
}

export async function getStockByProductId(productId: number): Promise<Stock | null> {
  const { data } = await http.get<ApiResponse<Stock>>(`/api/stocks/product/${productId}`)
  if (data.code !== 0) return null
  return data.data ?? null
}

