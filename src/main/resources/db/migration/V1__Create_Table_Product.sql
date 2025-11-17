CREATE TABLE IF NOT EXISTS `products` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(250) NOT NULL,
    `price` double NOT NULL,
    `description` varchar(250) NOT NULL,
    `quantity` int NOT NULL,
    `enabled` BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (`id`),
    CONSTRAINT chk_price_positive CHECK (`price` > 0),
    INDEX idx_name (`name`),
    INDEX idx_price (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;