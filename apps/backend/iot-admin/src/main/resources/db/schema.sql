-- Product categories supported by the IoT platform.
-- MySQL 8.0+
CREATE TABLE IF NOT EXISTS `product_category`
(
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `parent_id`    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Parent category ID; 0 means root category',
    `category_code` VARCHAR(64) NOT NULL COMMENT 'Stable category code used by integrations',
    `category_name` VARCHAR(100) NOT NULL COMMENT 'Display name',
    `description`  VARCHAR(500) NULL COMMENT 'Category description',
    `icon_url`     VARCHAR(500) NULL COMMENT 'Category icon URL',
    `sort_order`   INT NOT NULL DEFAULT 0 COMMENT 'Display order; smaller values come first',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    `is_deleted`   TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 deleted, 0 active',
    `created_by`   BIGINT UNSIGNED NULL COMMENT 'Creator user ID',
    `updated_by`   BIGINT UNSIGNED NULL COMMENT 'Last updater user ID',
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_category_code` (`category_code`),
    KEY `idx_product_category_parent` (`parent_id`),
    KEY `idx_product_category_status` (`status`, `is_deleted`),
    KEY `idx_product_category_sort` (`parent_id`, `sort_order`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'IoT product categories';
