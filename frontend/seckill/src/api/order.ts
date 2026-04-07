import { http } from './http'
import type { ApiResponse, DuplicateSeckill, Order, SeckillAccepted } from './types'

export async function seckill(input: { userId: number; productId: number; amount: number }): Promise<SeckillAccepted | DuplicateSeckill> {
  const { data } = await http.post<ApiResponse<any>>('/api/orders/seckill', input)
  if (data.code !== 0) throw new Error(data.message || 'seckill failed')
  return data.data
}

export async function getOrderByNo(orderNo: number): Promise<Order | null> {
  const { data } = await http.get<ApiResponse<Order>>(`/api/orders/by-no/${orderNo}`)
  if (data.code !== 0) return null
  return data.data ?? null
}

export async function listOrdersByUser(userId: number): Promise<Order[]> {
  const { data } = await http.get<ApiResponse<Order[]>>(`/api/orders/user/${userId}`)
  return data.data ?? []
}

