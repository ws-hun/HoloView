export type HotSource = 'WEIBO' | 'ZHIHU' | 'BAIDU' | 'DOUYIN' | 'TOUTIAO' | 'BILIBILI' | 'JUEJIN' | 'THE_PAPER' | 'ITHOME' | 'KR36' | 'JIN10' | 'HACKER_NEWS' | 'OTHER'
export type HotCategory =
  | 'GENERAL'
  | 'SOCIETY'
  | 'TECHNOLOGY'
  | 'ENTERTAINMENT'
  | 'SPORTS'
  | 'FINANCE'
  | 'INTERNATIONAL'
  | 'GAMING'
  | 'AUTOMOTIVE'
  | 'LIFESTYLE'
export type HotTrend = 'UP' | 'DOWN' | 'NEW' | 'STABLE'

export interface HotItem {
  id: number
  title: string
  description: string
  source: HotSource
  sourceName: string
  sourceUrl: string
  category: HotCategory
  categoryName: string
  hotValue: number
  hotValueText: string
  rank: number
  previousRank: number | null
  rankChange: number
  trend: HotTrend
  cover: string
  publishedAt: string | null
  updatedAt: string
}

export interface HotDetail {
  item: HotItem
  trendPoints: TrendPoint[]
  relatedItems: HotItem[]
}

export interface TrendPoint {
  hotValue: number
  rank: number
  recordedAt: string
}

export interface PlatformMeta {
  code: HotSource
  name: string
  shortName: string
  color: string
}

export interface CategoryMeta {
  code: HotCategory
  name: string
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface HotNotification {
  id: string
  title: string
  message: string
  createdAt: string
}
