import { http } from '@/api/http'
import { buildTrend, categories, mockHotItems, platforms } from '@/api/mockData'
import type { ApiResult, CategoryMeta, HotCategory, HotDetail, HotItem, HotSource, PlatformMeta, TrendPoint } from '@/types/hot'

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
  search(keyword: string, limit = 50) {
    return withMock(
      async () => (await http.get<ApiResult<HotItem[]>>('/hot/search', { params: { keyword, limit } })).data.data,
      () => {
        const normalized = keyword.trim().toLowerCase()
        return mockHotItems
          .filter((item) => `${item.title}${item.description}`.toLowerCase().includes(normalized))
          .sort((a, b) => b.hotValue - a.hotValue)
          .slice(0, limit)
      },
    )
  },
  detail(id: number) {
    return withMock(
      async () => (await http.get<ApiResult<HotDetail>>(`/hot/${id}`)).data.data.item,
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
      async () => (await http.get<ApiResult<PlatformMeta[]>>('/platform/list')).data.data.map((platform) => ({
        ...platform,
        shortName: platform.shortName || platform.name.replace(/(热搜|热榜|热点)$/, ''),
        color: platform.color || '#7c858b',
      })),
      () => platforms,
    )
  },
  categories() {
    return withMock(
      async () => (await http.get<ApiResult<CategoryMeta[]>>('/category/list')).data.data,
      () => categories,
    )
  },
  categoryHot(category: HotCategory, limit = 50) {
    return withMock(
      async () => (await http.get<ApiResult<HotItem[]>>(`/category/${category}/hot`, { params: { limit } })).data.data,
      () => mockHotItems
        .filter((item) => category === 'GENERAL' || item.category === category)
        .sort((a, b) => a.rank - b.rank || b.hotValue - a.hotValue)
        .slice(0, limit),
    )
  },
}
