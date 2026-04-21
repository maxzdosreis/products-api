CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_name VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(1000) NULL,
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    confirmed_at DATETIME NULL,
    shipped_at DATETIME NULL,
    delivered_at DATETIME NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_so_status ON sales_orders(status);
CREATE INDEX idx_so_created_at ON sales_orders(created_at);

CREATE TABLE IF NOT EXISTS sales_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sales_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_soi_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_soi_order ON sales_order_items(sales_order_id);
CREATE INDEX idx_soi_product ON sales_order_items(product_id);