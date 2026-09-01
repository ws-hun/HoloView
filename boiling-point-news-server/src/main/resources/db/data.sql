-- Boiling Point News - initial dictionary data
-- The upserts make this script safe to run repeatedly.

SET NAMES utf8mb4;

INSERT INTO `source_platform` (`name`, `code`, `logo`, `status`, `sort`)
VALUES
    ('微博', 'WEIBO', NULL, 1, 10),
    ('知乎', 'ZHIHU', NULL, 1, 20),
    ('百度', 'BAIDU', NULL, 1, 30),
    ('抖音', 'DOUYIN', NULL, 1, 40),
    ('今日头条', 'TOUTIAO', NULL, 1, 50),
    ('哔哩哔哩', 'BILIBILI', NULL, 1, 60),
    ('掘金', 'JUEJIN', NULL, 1, 70),
    ('澎湃新闻', 'THE_PAPER', NULL, 1, 80),
    ('IT之家', 'ITHOME', NULL, 1, 81),
    ('36氪', 'KR36', NULL, 1, 82),
    ('金十数据', 'JIN10', NULL, 1, 83),
    ('Hacker News', 'HACKER_NEWS', NULL, 1, 84),
    ('华尔街见闻', 'WALLSTREET_CN', NULL, 1, 85),
    ('牛客', 'NOWCODER', NULL, 1, 86),
    ('百度贴吧', 'TIEBA', NULL, 1, 87),
    ('虎扑', 'HUPU', NULL, 1, 88),
    ('豆瓣电影', 'DOUBAN_MOVIE', NULL, 1, 89),
    ('GitHub Trending', 'GITHUB_TRENDING', NULL, 1, 90),
    ('Steam', 'STEAM', NULL, 1, 91),
    ('什么值得买', 'SMZDM', NULL, 1, 92),
    ('懂球帝', 'DONGQIUDI', NULL, 1, 93),
    ('其他', 'OTHER', NULL, 1, 99) AS `incoming`
ON DUPLICATE KEY UPDATE
    `name` = `incoming`.`name`,
    `logo` = `incoming`.`logo`,
    `status` = `incoming`.`status`,
    `sort` = `incoming`.`sort`;

INSERT INTO `hot_category` (`name`, `code`, `status`, `sort`)
VALUES
    ('综合', 'GENERAL', 1, 10),
    ('社会', 'SOCIETY', 1, 20),
    ('科技', 'TECHNOLOGY', 1, 30),
    ('娱乐', 'ENTERTAINMENT', 1, 40),
    ('体育', 'SPORTS', 1, 50),
    ('财经', 'FINANCE', 1, 60),
    ('国际', 'INTERNATIONAL', 1, 70),
    ('游戏', 'GAMING', 1, 80),
    ('汽车', 'AUTOMOTIVE', 1, 90),
    ('生活', 'LIFESTYLE', 1, 100) AS `incoming`
ON DUPLICATE KEY UPDATE
    `name` = `incoming`.`name`,
    `status` = `incoming`.`status`,
    `sort` = `incoming`.`sort`;
