-- Adiciona category_id em products (nullable para permitir migração segura)
ALTER TABLE products
    ADD COLUMN category_id BIGINT NULL;

-- Associar todos os produtos existentes à categoria 'Geral'
UPDATE products
SET category_id = (SELECT id_category FROM categories WHERE name_category = 'Geral');

-- Torna category_id obrigatório e adiciona FK
ALTER TABLE products
    MODIFY COLUMN category_id BIGINT NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT fk_product_category
    FOREIGN KEY (category_id) REFERENCES categories(id_category);

CREATE INDEX idx_product_category ON products(category_id);

-- Cria tabela de movimentação de estoque
CREATE TABLE IF NOT EXISTS stock_movements (
    id_stock_movements BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    quantity DECIMAL(19, 3) NOT NULL,
    stock_before DECIMAL(19, 3) NOT NULL,
    stock_after DECIMAL (19, 3) NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id_stock_movements),
    CONSTRAINT fk_sm_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_sm_product ON stock_movements(product_id);
CREATE INDEX idx_sm_created_at ON stock_movements(created_at);
CREATE INDEX idx_sm_type ON stock_movements(type);