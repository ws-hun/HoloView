# 沸点速报

> 把全网正在发生的事，放在一起看。

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3-42B883?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Status](https://img.shields.io/badge/status-active_development-e72b31)](#开发状态)

沸点速报是一个面向普通用户的实时热点聚合项目。它把微博、知乎、百度、抖音和今日头条的热搜统一成一套可比较的热度、排名和趋势数据，让用户打开页面就能知道“现在大家都在讨论什么”。

当前版本使用 Mock Collector 模拟多平台热点变化，已经具备完整的前后端分层、数据库模型、榜单查询、趋势曲线、SSE 实时更新和响应式前端。后续接入真实平台时，只需要新增数据源适配器，不需要重写页面。

![沸点速报首页预览](docs/preview.jpg)

## 特性

- 综合热榜：按统一热度排序，展示排名、排名变化、来源和更新时间。
- 多平台榜单：微博、知乎、百度、抖音、今日头条独立查看。
- 分类浏览：综合、社会、科技、娱乐、体育、财经、国际、游戏、汽车、生活。
- 热点详情：原始来源、当前热度、排名变化、24 小时趋势和相关热点。
- 实时更新：SSE 事件流 + Pinia 状态，Mock 模式下自动模拟热度变化。
- 可替换采集器：基于 `HotSearchCollector` 接口扩展新平台。
- 可排查日志：前后端记录请求、查询、采集、缓存、SSE 连接和异常上下文。
- 真实产品取向：资讯产品布局，不使用后台管理模板，不依赖复杂 UI 组件库。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| Web | Vue 3、Vite、TypeScript、Vue Router、Pinia、Tailwind CSS |
| 可视化 | ECharts（按需注册） |
| 交互 | Axios、原生 EventSource、Lucide Icons |
| Server | Java 17、Spring Boot 3.5、Spring MVC、MyBatis-Plus |
| Storage | MySQL 8、Redis |
| Test | JUnit 5、Spring Boot Test、MockMvc、H2 |

## 项目结构

```text
.
├── boiling-point-news-server/       # Spring Boot API、采集与实时推送
│   ├── src/main/java/com/boilingpoint/news/
│   │   ├── common/                  # Result、枚举和通用约定
│   │   ├── config/                  # MyBatis 等配置
│   │   ├── controller/              # REST / SSE 接口（持续建设中）
│   │   ├── converter/               # Entity / DTO / VO 转换
│   │   ├── dto/                     # 请求参数
│   │   ├── entity/                  # 数据库实体
│   │   ├── exception/               # 业务异常和全局处理
│   │   ├── mapper/                  # MyBatis-Plus Mapper
│   │   ├── service/                 # 查询服务
│   │   └── collector/               # 数据源适配器（持续建设中）
│   └── src/main/resources/
│       ├── db/                      # schema.sql、data.sql
│       └── mapper/                  # MyBatis XML
├── boiling-point-news-web/          # Vue 3 前端
│   └── src/
│       ├── api/                     # Axios、API 和 Mock 数据
│       ├── components/              # 榜单、Tab、趋势图等
│       ├── layouts/                 # 全站布局
│       ├── router/                  # 页面路由
│       ├── stores/                  # Pinia 热点状态
│       ├── utils/                   # 格式化、日志
│       └── views/                   # 首页、热榜、分类、趋势、详情
└── docs/preview.jpg                 # GitHub 项目预览图
```

## 快速开始

### 1. 前端

```bash
cd boiling-point-news-web
npm install
npm run dev
```

打开 <http://127.0.0.1:5173/>。

默认使用 Mock 数据，不依赖 MySQL、Redis 或后端即可浏览完整页面。

常用命令：

```bash
npm run build       # 类型检查 + 生产构建
npm run type-check  # 仅 TypeScript 检查
npm run preview     # 预览生产构建
```

### 2. 后端

环境要求：Java 17、Maven 3.9+、MySQL 8、Redis 6+。

先创建数据库并执行：

```bash
mysql -uroot -p boiling_point_news < boiling-point-news-server/src/main/resources/db/schema.sql
mysql -uroot -p boiling_point_news < boiling-point-news-server/src/main/resources/db/data.sql
```

再启动服务：

```bash
cd boiling-point-news-server
mvn spring-boot:run
```

默认服务地址为 <http://127.0.0.1:8080/>。数据库和 Redis 连接信息通过环境变量配置：

```bash
export DB_URL='jdbc:mysql://localhost:3306/boiling_point_news?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false'
export DB_USERNAME='root'
export DB_PASSWORD='your-password'
export REDIS_HOST='localhost'
export REDIS_PORT='6379'
```

### 3. 切换前端到后端 API

复制前端环境模板：

```bash
cd boiling-point-news-web
cp .env.example .env.local
```

修改为：

```text
VITE_DATA_MODE=live
VITE_API_BASE_URL=/api
VITE_SSE_URL=/api/sse/hot
```

## 页面路由

| 路由 | 页面 |
| --- | --- |
| `/` | 首页与综合实时热榜 |
| `/boards` | 平台热榜 |
| `/category/:code` | 分类热点 |
| `/trends` | 热度趋势 |
| `/hot/:id` | 热点详情 |

## 数据与接口约定

热点统一模型包含：`title`、`source`、`category`、`hotValue`、`rank`、`previousRank`、`rankChange`、`trend`、`publishedAt` 和 `updatedAt`。

后端 REST 响应统一使用：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

核心接口规划：

```text
GET /api/hot/list
GET /api/hot/{id}
GET /api/hot/ranking
GET /api/hot/trending
GET /api/hot/latest
GET /api/hot/search?keyword=人工智能
GET /api/hot/{id}/trend?hours=24&limit=100
GET /api/platform/list
GET /api/platform/{code}/hot
GET /api/category/list
GET /api/category/{code}/hot
GET /api/sse/hot
```

## 日志与排查

项目从第一阶段就保留可观察性：

- 后端使用 SLF4J，记录查询条件、结果数量、耗时、采集平台、SSE 连接和异常堆栈。
- 热点列表、详情、最新榜和趋势查询使用 Redis Cache-Aside；缓存不可用时自动回源数据库，并记录缓存读写告警。
- 每次 HTTP 请求都会生成或透传 `X-Request-Id`，响应头和日志均带该 ID，便于前后端串联排查。
- 开发环境启用 MyBatis SQL 日志，便于检查 SQL、参数和结果。
- 前端统一输出 `[沸点速报]` 前缀日志，记录 API 失败、SSE 连接/重连和 Mock 更新。
- 日志不包含密码、Token、完整连接串或其他敏感信息。

## 开发状态

已完成：

- 数据库表、索引和初始化字典。
- Spring Boot 基础工程、统一响应和全局异常处理。
- Entity、DTO、VO、Converter。
- MyBatis-Plus Mapper 与 H2 集成测试。
- 热点查询、搜索、排行、趋势和平台/分类 Service。
- REST Controller、接口参数校验与请求链路日志。
- Redis Cache-Aside 缓存服务。
- Mock Collector、Collector Registry、定时采集、热点 upsert、排名变化计算和历史快照写入。
- SSE 服务端连接管理、心跳和采集完成事件推送。
- Vue 首页、热榜、分类、趋势、详情、搜索和 Mock 实时流。

进行中：

- 前后端 Live 模式接口联调（REST + SSE）。
- SSE 服务端推送。
- MySQL、Redis、前后端一键启动配置。

## 路线图

1. 接入第一个真实平台数据源，并增加失败降级和限流。
2. 增加关键词订阅、收藏、AI 摘要和舆情聚类。

## 贡献

欢迎提交 Issue 或 Pull Request。新增平台时请实现统一的 `HotSearchCollector` 接口，并补充：

- 采集失败和空数据测试。
- 标准化字段映射说明。
- 关键路径日志。
- 前端来源颜色和平台文案。

## License

本项目暂未选择开源许可证。若用于公开分发或商业使用，请先补充合适的 License。
