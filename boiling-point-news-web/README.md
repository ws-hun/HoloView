# 沸点速报 Web

## 本地运行

```bash
npm install
npm run dev
```

默认访问 `http://127.0.0.1:5173/`。

## 数据模式

默认使用本地 Mock 热点流，便于后端尚未完成时先验收页面、路由和实时状态。

复制 `.env.example` 为 `.env.local`，并设置：

```text
VITE_DATA_MODE=live
VITE_API_BASE_URL=/api
VITE_SSE_URL=/api/sse/hot
```

切换到 `live` 后，前端会调用 Spring Boot REST API 和 SSE；请求失败会记录到浏览器控制台的 `[沸点速报]` 日志中。

## 页面

- `/` 首页与实时综合热榜
- `/boards` 平台热榜
- `/category/:code` 分类热榜
- `/trends` 热度趋势
- `/hot/:id` 热点详情
