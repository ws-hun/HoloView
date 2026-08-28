-- Boiling Point News - MySQL 8.x schema
-- Run this script against the application database before data.sql.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `source_platform` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(50) NOT NULL COMMENT 'Display name',
    `code` VARCHAR(32) NOT NULL COMMENT 'Platform code',
    `logo` VARCHAR(1024) DEFAULT NULL COMMENT 'Logo URL',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    `sort` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_source_platform_code` (`code`),
    KEY `idx_source_platform_status_sort` (`status`, `sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Hot news source platforms';

CREATE TABLE IF NOT EXISTS `hot_category` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(50) NOT NULL COMMENT 'Display name',
    `code` VARCHAR(32) NOT NULL COMMENT 'Category code',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    `sort` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hot_category_code` (`code`),
    KEY `idx_hot_category_status_sort` (`status`, `sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Hot news categories';

CREATE TABLE IF NOT EXISTS `hot_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `title` VARCHAR(255) NOT NULL COMMENT 'Hot topic title',
    `description` TEXT DEFAULT NULL COMMENT 'Topic summary',
    `source` VARCHAR(32) NOT NULL COMMENT 'Source platform code',
    `source_item_key` VARCHAR(128) NOT NULL COMMENT 'Stable item identifier from the source',
    `source_url` VARCHAR(1024) DEFAULT NULL COMMENT 'Original topic URL',
    `category` VARCHAR(32) NOT NULL DEFAULT 'GENERAL' COMMENT 'Category code',
    `hot_value` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Normalized hot value',
    `hot_value_text` VARCHAR(32) NOT NULL DEFAULT '0' COMMENT 'Original or formatted hot value',
    `rank` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Current source rank; 0 means unranked',
    `previous_rank` INT UNSIGNED DEFAULT NULL COMMENT 'Rank from the previous collection',
    `rank_change` INT NOT NULL DEFAULT 0 COMMENT 'Positive means rank moved up',
    `trend` VARCHAR(16) NOT NULL DEFAULT 'STABLE' COMMENT 'UP, DOWN, NEW or STABLE',
    `cover` VARCHAR(1024) DEFAULT NULL COMMENT 'Cover image URL',
    `published_at` DATETIME(3) DEFAULT NULL COMMENT 'Original publish time',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0 offline, 1 active',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Logical deletion flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hot_item_source_key` (`source`, `source_item_key`),
    KEY `idx_hot_item_global_ranking` (`deleted`, `status`, `hot_value` DESC),
    KEY `idx_hot_item_source_ranking` (`source`, `deleted`, `status`, `rank`),
    KEY `idx_hot_item_category_ranking` (`category`, `deleted`, `status`, `hot_value` DESC),
    KEY `idx_hot_item_trend_value` (`trend`, `deleted`, `status`, `hot_value` DESC),
    KEY `idx_hot_item_updated_at` (`updated_at` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Current hot news snapshot';

CREATE TABLE IF NOT EXISTS `hot_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `hot_id` BIGINT UNSIGNED NOT NULL COMMENT 'Related hot_item id',
    `hot_value` BIGINT UNSIGNED NOT NULL COMMENT 'Hot value at collection time',
    `rank` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Rank at collection time',
    `recorded_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        COMMENT 'Collection time',
    PRIMARY KEY (`id`),
    KEY `idx_hot_history_hot_recorded` (`hot_id`, `recorded_at` DESC),
    KEY `idx_hot_history_recorded_at` (`recorded_at`),
    CONSTRAINT `fk_hot_history_hot_item`
        FOREIGN KEY (`hot_id`) REFERENCES `hot_item` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Hot value and rank history';
