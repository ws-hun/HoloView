INSERT INTO source_platform (name, code, status, sort) VALUES
('微博', 'WEIBO', 1, 10),
('知乎', 'ZHIHU', 1, 20),
('百度', 'BAIDU', 1, 30),
('抖音', 'DOUYIN', 1, 40),
('今日头条', 'TOUTIAO', 1, 50),
('其他', 'OTHER', 1, 99);

INSERT INTO hot_category (name, code, status, sort) VALUES
('综合', 'GENERAL', 1, 10),
('社会', 'SOCIETY', 1, 20),
('科技', 'TECHNOLOGY', 1, 30),
('娱乐', 'ENTERTAINMENT', 1, 40),
('体育', 'SPORTS', 1, 50),
('财经', 'FINANCE', 1, 60),
('国际', 'INTERNATIONAL', 1, 70),
('游戏', 'GAMING', 1, 80),
('汽车', 'AUTOMOTIVE', 1, 90),
('生活', 'LIFESTYLE', 1, 100);

INSERT INTO hot_item
(id, title, description, source, source_item_key, source_url, category, hot_value, hot_value_text,
 `rank`, previous_rank, rank_change, trend, cover, status, deleted, updated_at)
VALUES
(101, '国产大模型推理能力迎来新突破', '多项复杂推理与长文本基准刷新纪录，产业应用进入新阶段。',
 'WEIBO', 'local-101', 'https://weibo.com', 'TECHNOLOGY', 98560000, '9856万', 1, 4, 3, 'UP',
 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=1000&q=82', 1, 0, CURRENT_TIMESTAMP),
(102, '多地发布新学期教育惠民举措', '围绕学生餐、课后服务和数字校园建设，多地公布保障方案。',
 'BAIDU', 'local-102', 'https://www.baidu.com', 'SOCIETY', 87320000, '8732万', 2, 2, 0, 'STABLE',
 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1000&q=82', 1, 0, DATEADD('MINUTE', -1, CURRENT_TIMESTAMP)),
(103, '新能源汽车月度交付再创新高', '补能效率与智能驾驶体验成为消费者关注重点。',
 'DOUYIN', 'local-103', 'https://www.douyin.com', 'AUTOMOTIVE', 75180000, '7518万', 3, NULL, 0, 'NEW',
 'https://images.unsplash.com/photo-1592833159155-c62df1b65634?auto=format&fit=crop&w=1000&q=82', 1, 0, DATEADD('MINUTE', -2, CURRENT_TIMESTAMP)),
(104, '中国队锁定世界大赛决赛席位', '多名年轻选手发挥稳定，团队配合成为取胜关键。',
 'TOUTIAO', 'local-104', 'https://www.toutiao.com', 'SPORTS', 69240000, '6924万', 4, 8, 4, 'UP',
 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=1000&q=82', 1, 0, DATEADD('MINUTE', -3, CURRENT_TIMESTAMP)),
(105, '城市更新行动加速推进', '多个城市以微改造方式完善公共空间并保留历史风貌。',
 'ZHIHU', 'local-105', 'https://www.zhihu.com', 'LIFESTYLE', 61770000, '6177万', 5, 3, -2, 'DOWN',
 'https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=1000&q=82', 1, 0, DATEADD('MINUTE', -4, CURRENT_TIMESTAMP));

INSERT INTO hot_history (hot_id, hot_value, `rank`, recorded_at) VALUES
(101, 62000000, 6, DATEADD('HOUR', -6, CURRENT_TIMESTAMP)),
(101, 74000000, 4, DATEADD('HOUR', -4, CURRENT_TIMESTAMP)),
(101, 86000000, 2, DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
(101, 98560000, 1, CURRENT_TIMESTAMP);
