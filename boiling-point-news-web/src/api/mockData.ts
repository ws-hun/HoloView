import type { CategoryMeta, HotItem, PlatformMeta, TrendPoint } from '@/types/hot'

const now = new Date('2026-08-27T17:40:00+08:00')
const minutesAgo = (minutes: number) => new Date(now.getTime() - minutes * 60_000).toISOString()

export const platforms: PlatformMeta[] = [
  { code: 'WEIBO', name: '微博热搜', shortName: '微博', color: '#e72b31' },
  { code: 'ZHIHU', name: '知乎热榜', shortName: '知乎', color: '#1772f6' },
  { code: 'BAIDU', name: '百度热搜', shortName: '百度', color: '#3b5bdb' },
  { code: 'DOUYIN', name: '抖音热点', shortName: '抖音', color: '#17191c' },
  { code: 'TOUTIAO', name: '今日头条', shortName: '头条', color: '#f04438' },
  { code: 'BILIBILI', name: '哔哩哔哩', shortName: 'B站', color: '#00aeec' },
  { code: 'JUEJIN', name: '掘金', shortName: '掘金', color: '#1e80ff' },
  { code: 'THE_PAPER', name: '澎湃新闻', shortName: '澎湃', color: '#1f6fb2' },
  { code: 'ITHOME', name: 'IT之家', shortName: 'IT之家', color: '#1677ff' },
  { code: 'KR36', name: '36氪', shortName: '36氪', color: '#222' },
  { code: 'JIN10', name: '金十数据', shortName: '金十', color: '#1769aa' },
  { code: 'HACKER_NEWS', name: 'Hacker News', shortName: 'HN', color: '#ff6600' },
]

export const categories: CategoryMeta[] = [
  { code: 'GENERAL', name: '综合' },
  { code: 'SOCIETY', name: '社会' },
  { code: 'TECHNOLOGY', name: '科技' },
  { code: 'ENTERTAINMENT', name: '娱乐' },
  { code: 'SPORTS', name: '体育' },
  { code: 'FINANCE', name: '财经' },
  { code: 'INTERNATIONAL', name: '国际' },
  { code: 'GAMING', name: '游戏' },
  { code: 'AUTOMOTIVE', name: '汽车' },
  { code: 'LIFESTYLE', name: '生活' },
]

const image = (id: string) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=1000&q=82`

export const mockHotItems: HotItem[] = [
  {
    id: 101,
    title: '国产大模型推理能力迎来新突破，多项基准刷新纪录',
    description: '新一代大模型在复杂推理、长文本理解和多模态任务上实现明显提升，产业应用进入新阶段。',
    source: 'WEIBO', sourceName: '微博', sourceUrl: 'https://weibo.com', category: 'TECHNOLOGY', categoryName: '科技',
    hotValue: 98_560_000, hotValueText: '9856万', rank: 1, previousRank: 4, rankChange: 3, trend: 'UP',
    cover: image('photo-1550751827-4bd374c3f58b'), publishedAt: minutesAgo(34), updatedAt: minutesAgo(1),
  },
  {
    id: 102,
    title: '多地发布新学期教育惠民举措，覆盖校园服务全场景',
    description: '围绕学生餐、课后服务和数字校园建设，多地陆续公布秋季新学期保障方案。',
    source: 'BAIDU', sourceName: '百度', sourceUrl: 'https://www.baidu.com', category: 'SOCIETY', categoryName: '社会',
    hotValue: 87_320_000, hotValueText: '8732万', rank: 2, previousRank: 2, rankChange: 0, trend: 'STABLE',
    cover: image('photo-1523240795612-9a054b0db644'), publishedAt: minutesAgo(58), updatedAt: minutesAgo(2),
  },
  {
    id: 103,
    title: '新能源汽车月度交付再创新高，智能化成为竞争焦点',
    description: '多家车企披露最新交付数据，补能效率与智能驾驶体验成为消费者关注重点。',
    source: 'DOUYIN', sourceName: '抖音', sourceUrl: 'https://www.douyin.com', category: 'AUTOMOTIVE', categoryName: '汽车',
    hotValue: 75_180_000, hotValueText: '7518万', rank: 3, previousRank: null, rankChange: 0, trend: 'NEW',
    cover: image('photo-1592833159155-c62df1b65634'), publishedAt: minutesAgo(21), updatedAt: minutesAgo(2),
  },
  {
    id: 104,
    title: '中国队锁定世界大赛决赛席位，年轻阵容表现亮眼',
    description: '关键场次中多名年轻选手发挥稳定，团队配合和临场调整成为取胜关键。',
    source: 'TOUTIAO', sourceName: '今日头条', sourceUrl: 'https://www.toutiao.com', category: 'SPORTS', categoryName: '体育',
    hotValue: 69_240_000, hotValueText: '6924万', rank: 4, previousRank: 8, rankChange: 4, trend: 'UP',
    cover: image('photo-1461896836934-ffe607ba8211'), publishedAt: minutesAgo(76), updatedAt: minutesAgo(3),
  },
  {
    id: 105,
    title: '城市更新行动加速，老街区焕发生活新活力',
    description: '多个城市以微改造方式完善公共空间，在保留历史风貌的同时提升居住体验。',
    source: 'ZHIHU', sourceName: '知乎', sourceUrl: 'https://www.zhihu.com', category: 'LIFESTYLE', categoryName: '生活',
    hotValue: 61_770_000, hotValueText: '6177万', rank: 5, previousRank: 3, rankChange: -2, trend: 'DOWN',
    cover: image('photo-1518005020951-eccb494ad742'), publishedAt: minutesAgo(104), updatedAt: minutesAgo(4),
  },
  {
    id: 106,
    title: '消费市场活力持续释放，服务消费热度显著上升',
    description: '文旅、餐饮和体育消费保持增长，新场景与新业态带动市场供需两旺。',
    source: 'BAIDU', sourceName: '百度', sourceUrl: 'https://www.baidu.com', category: 'FINANCE', categoryName: '财经',
    hotValue: 57_960_000, hotValueText: '5796万', rank: 6, previousRank: 7, rankChange: 1, trend: 'UP',
    cover: image('photo-1556742049-0cfed4f6a45d'), publishedAt: minutesAgo(140), updatedAt: minutesAgo(5),
  },
  {
    id: 107,
    title: '年度口碑剧集收官，现实题材引发广泛共鸣',
    description: '细腻的人物刻画与真实生活议题成为讨论焦点，主创分享幕后创作故事。',
    source: 'WEIBO', sourceName: '微博', sourceUrl: 'https://weibo.com', category: 'ENTERTAINMENT', categoryName: '娱乐',
    hotValue: 52_430_000, hotValueText: '5243万', rank: 7, previousRank: 5, rankChange: -2, trend: 'DOWN',
    cover: image('photo-1485846234645-a62644f84728'), publishedAt: minutesAgo(89), updatedAt: minutesAgo(6),
  },
  {
    id: 108,
    title: '全球科技创新合作论坛今天开幕，聚焦开放生态',
    description: '来自多个国家和地区的科研机构与企业代表围绕人工智能、绿色能源展开交流。',
    source: 'TOUTIAO', sourceName: '今日头条', sourceUrl: 'https://www.toutiao.com', category: 'INTERNATIONAL', categoryName: '国际',
    hotValue: 48_810_000, hotValueText: '4881万', rank: 8, previousRank: 10, rankChange: 2, trend: 'UP',
    cover: image('photo-1521295121783-8a321d551ad2'), publishedAt: minutesAgo(47), updatedAt: minutesAgo(7),
  },
  {
    id: 109,
    title: '新一代国产游戏正式定档，东方美学设计受关注',
    description: '制作团队公布实机演示，场景设计与音乐表达呈现出鲜明的东方文化特色。',
    source: 'DOUYIN', sourceName: '抖音', sourceUrl: 'https://www.douyin.com', category: 'GAMING', categoryName: '游戏',
    hotValue: 43_670_000, hotValueText: '4367万', rank: 9, previousRank: null, rankChange: 0, trend: 'NEW',
    cover: image('photo-1542751371-adc38448a05e'), publishedAt: minutesAgo(16), updatedAt: minutesAgo(8),
  },
  {
    id: 110,
    title: '社区食堂推出时令菜单，便民服务再升级',
    description: '针对老年人和上班族的不同需求，社区食堂延长服务时段并上线小份菜。',
    source: 'ZHIHU', sourceName: '知乎', sourceUrl: 'https://www.zhihu.com', category: 'LIFESTYLE', categoryName: '生活',
    hotValue: 38_920_000, hotValueText: '3892万', rank: 10, previousRank: 6, rankChange: -4, trend: 'DOWN',
    cover: image('photo-1556911220-bff31c812dba'), publishedAt: minutesAgo(128), updatedAt: minutesAgo(9),
  },
  {
    id: 111,
    title: '低空经济应用场景扩容，物流与应急救援率先落地',
    description: '多个试点区域探索低空航线常态化运营，安全标准与基础设施同步完善。',
    source: 'ZHIHU', sourceName: '知乎', sourceUrl: 'https://www.zhihu.com', category: 'TECHNOLOGY', categoryName: '科技',
    hotValue: 35_850_000, hotValueText: '3585万', rank: 11, previousRank: 15, rankChange: 4, trend: 'UP',
    cover: image('photo-1508614589041-895b88991e3e'), publishedAt: minutesAgo(190), updatedAt: minutesAgo(10),
  },
  {
    id: 112,
    title: '博物馆夜游持续升温，文化体验打开新方式',
    description: '夜间展览、沉浸式导览与文创市集吸引年轻观众，暑期文化消费持续火热。',
    source: 'WEIBO', sourceName: '微博', sourceUrl: 'https://weibo.com', category: 'LIFESTYLE', categoryName: '生活',
    hotValue: 31_640_000, hotValueText: '3164万', rank: 12, previousRank: 12, rankChange: 0, trend: 'STABLE',
    cover: image('photo-1564399579883-451a5d44ec08'), publishedAt: minutesAgo(220), updatedAt: minutesAgo(12),
  },
]

export function buildTrend(item: HotItem, points = 12): TrendPoint[] {
  return Array.from({ length: points }, (_, index) => {
    const distance = points - index - 1
    const growth = item.trend === 'DOWN' ? 1 + distance * 0.035 : 1 - distance * 0.045
    const noise = 1 + Math.sin(index * 1.7 + item.id) * 0.035
    return {
      hotValue: Math.max(1_000_000, Math.round(item.hotValue * growth * noise)),
      rank: Math.max(1, item.rank + Math.round(Math.sin(index + item.id) * 2)),
      recordedAt: new Date(new Date(item.updatedAt).getTime() - distance * 2 * 60 * 60_000).toISOString(),
    }
  })
}
