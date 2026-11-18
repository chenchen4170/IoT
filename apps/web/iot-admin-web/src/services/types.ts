export type ApiResponse<T> = {
  code: number
  traceId: string
  msg: string
  data: T | null
}
