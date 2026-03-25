-- Adiciona type
ALTER TABLE products
    ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PRODUCT' AFTER description;

-- Adiciona unit
ALTER TABLE products
    ADD COLUMN unit VARCHAR(10) NULL AFTER type;

-- Adiciona cost_price (inicia com o mesmo valor de price)
ALTER TABLE products
    ADD COLUMN cost_price DECIMAL(19,2) NULL AFTER unit;

UPDATE products SET cost_price = price WHERE cost_price IS NULL;

-- Adiciona sale_price (inicia com o mesmo valor de price)
ALTER TABLE products
    ADD COLUMN sale_price DECIMAL(19,2) NULL AFTER cost_price;

UPDATE products SET sale_price = price WHERE sale_price IS NULL;

-- Adiciona os campos de estoque
ALTER TABLE products
    ADD COLUMN min_stock DECIMAL(19,3) NULL AFTER sale_price,
    ADD COLUMN max_stock DECIMAL(19,3) NULL AFTER min_stock,
    ADD COLUMN current_stock DECIMAL(19,3) NOT NULL DEFAULT 0.000 AFTER max_stock;

-- Migrar quantity para current_stock nos registros existentes
UPDATE products SET current_stock = quantity WHERE quantity IS NOT NULL;

-- Adiciona flags de controle
ALTER TABLE products
    ADD COLUMN requires_batch_control BOOLEAN NOT NULL DEFAULT FALSE AFTER current_stock;

ALTER TABLE products
    ADD COLUMN requires_expiry_control BOOLEAN NOT NULL DEFAULT FALSE AFTER requires_batch_control;

-- Remove a coluna price (substituída por cost_price e sale_price)
ALTER TABLE products DROP COLUMN price;

-- Remove a coluna quantity (substituída por current_stock)
ALTER TABLE products DROP COLUMN quantity;

-- Novos índices
CREATE INDEX idx_product_id ON products(id);

CREATE INDEX idx_product_type ON products(type);
