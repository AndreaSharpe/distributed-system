export type ApiResponse<T> = {
  code: number
  message: string
  data?: T
}

export type Product = {
  id: number
  name: string
  price: number
  stock: number
  description?: string
  createdAt?: string
}

export type Stock = {
  id: number
  productId: number
  quantity: number
  updatedAt?: string
}

export type Order = {
  id?: number
  orderNo?: number
  userId: number
  productId: number
  stockId: number
  amount: number
  status: string
  createdAt?: string
}

export type SeckillAccepted = {
  orderNo: number
  userId: number
  productId: number
  stockId: number
  amount: number
  status: 'accepted'
}

export type DuplicateSeckill = {
  orderNo: number
  duplicate: true
  order?: Order | null
}

