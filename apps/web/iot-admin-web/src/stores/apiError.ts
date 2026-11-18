import { create } from 'zustand'

type ApiErrorNotice = {
  id: number
  title: string
}

type ApiErrorState = {
  notice: ApiErrorNotice | null
  show: (title: string) => void
  clear: (id: number) => void
}

let nextId = 0

export const useApiError = create<ApiErrorState>((set) => ({
  notice: null,
  show: (title) => set({ notice: { id: ++nextId, title } }),
  clear: (id) => set((state) => state.notice?.id === id ? { notice: null } : state),
}))
