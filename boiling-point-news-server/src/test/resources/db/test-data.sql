INSERT INTO source_platform (name, code, status, sort) VALUES
('微博', 'WEIBO', 1, 10),
('知乎', 'ZHIHU', 1, 20),
('停用平台', 'OTHER', 0, 99);

INSERT INTO hot_category (name, code, status, sort) VALUES
('综合', 'GENERAL', 1, 10),
('科技', 'TECHNOLOGY', 1, 20),
('停用分类', 'LIFESTYLE', 0, 99);

INSERT INTO hot_item
(id, title, description, source, source_item_key, category, hot_value, hot_value_text, `rank`, previous_rank, rank_change, trend, status, deleted, updated_at)
VALUES
(101, '国产大模型推理能力突破', '人工智能行业最新进展', 'WEIBO', 'weibo-101', 'TECHNOLOGY', 98560000, '9856万', 1, 4, 3, 'UP', 1, 0, '2026-08-27 17:39:00'),
(102, '城市公共服务升级', '多地发布便民措施', 'ZHIHU', 'zhihu-102', 'SOCIETY', 87320000, '8732万', 2, 2, 0, 'STABLE', 1, 0, '2026-08-27 17:38:00'),
(103, '低空经济应用扩容', '科技产业落地加速', 'ZHIHU', 'zhihu-103', 'TECHNOLOGY', 35850000, '3585万', 11, 15, 4, 'UP', 1, 0, '2026-08-27 17:30:00'),
(104, '已下线热点', '不应出现在榜单', 'WEIBO', 'weibo-104', 'TECHNOLOGY', 99999999, '9999万', 1, 1, 0, 'STABLE', 0, 0, '2026-08-27 17:40:00'),
(105, '已删除热点', '不应出现在榜单', 'WEIBO', 'weibo-105', 'TECHNOLOGY', 99999999, '9999万', 1, 1, 0, 'STABLE', 1, 1, '2026-08-27 17:40:00');

INSERT INTO hot_history (hot_id, hot_value, `rank`, recorded_at) VALUES
(101, 30000000, 8, '2026-08-27 10:00:00'),
(101, 42000000, 6, '2026-08-27 12:00:00'),
(101, 56000000, 4, '2026-08-27 14:00:00'),
(101, 78000000, 2, '2026-08-27 16:00:00');
