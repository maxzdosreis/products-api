CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
  `id` BIGINT AUTO_INCREMENT,
  `token` VARCHAR(255) NOT NULL UNIQUE,
  `email` VARCHAR(255) NOT NULL,
  `expiry_date` DATETIME NOT NULL,
  `used` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX idx_token (`token`),
  INDEX idx_email (`email`),
  INDEX idx_expiry_date (`expiry_date`),
  INDEX idx_used (`used`),
  INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB;