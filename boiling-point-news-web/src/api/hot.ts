import { http } from '@/api/http'
import { buildTrend, categories, mockHotItems, platforms } from '@/api/mockData'
import type { ApiResult, CategoryMeta, HotCategory, HotItem, HotSource, PlatformMeta, TrendPoint } from '@/types/hot'

const isMock = import.meta.env.VITE_DATA_MODE !== 'live'

async function withMock<T>(request: () => Promise<T>, fallback: () => T): Promise<T> {
  if (isMock) return Promise.resolve(fallback())
  return request()
}

export const hotApi = {
  list(params?: { source?: HotSource; category?: HotCategory; keyword?: string; limit?: number }) {
    return withMock(
      async () => (await http.get<ApiResult<HotItem[]>>('/hot/list', { params })).data.data,
      () => {
        let items = [...mockHotItems]
        if (params?.source) items = items.filter((item) => item.source === params.source)
        if (params?.category && params.category !== 'GENERAL') items = items.filter((item) => item.category === params.category)
        if (params?.keyword) {
          const keyword = params.keyword.trim().toLowerCase()
          items = items.filter((item) => `${item.title}${item.description}`.toLowerCase().includes(keyword))
        }
        return items.slice(0, params?.limit || 100)
      },
    )
  },
  detail(id: number) {
    return withMock(
      async () => (await http.get<ApiResult<HotItem>>(`/hot/${id}`)).data.data,
      () => mockHotItems.find((item) => item.id === id) || mockHotItems[0],
    )
  },
  trend(id: number) {
    return withMock(
      async () => (await http.get<ApiResult<TrendPoint[]>>(`/hot/${id}/trend`)).data.data,
      () => buildTrend(mockHotItems.find((item) => item.id === id) || mockHotItems[0]),
    )
  },
  platforms() {
    return withMock(
      async () => (await http.get<ApiResult<PlatformMeta[]>>('/platform/list')).data.data,
      () => platforms,
    )
  },
  categories() {
    return withMock(
      async () => (await http.get<ApiResult<CategoryMeta[]>>('/category/list')).data.data,
      () => categories,
    )
  },
}
