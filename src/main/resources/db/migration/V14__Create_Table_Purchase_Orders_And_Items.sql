CREATE TABLE IF NOT EXISTS purchase_orders (
    id_po BIGINT NOT NULL AUTO_INCREMENT,
    supplier_name VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(1000) NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    confirmed_at DATETIME NOT NULL,
    received_at DATETIME NOT NULL,
    PRIMARY KEY (id_po)
);

CREATE INDEX idx_po_status ON purchase_orders(status);
CREATE INDEX idx_po_created_at ON purchase_orders(created_at);

CREATE TABLE IF NOT EXISTS purchase_orders_items (
    id_poi BIGINT NOT NULL AUTO_INCREMENT,
    purchase_orders_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    poi_quantity DECIMAL(19,3) NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    PRIMARY KEY (id_poi),
    CONSTRAINT fk_poi_order FOREIGN KEY (purchase_orders_id) REFERENCES purchase_orders(id_po),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_poi_order ON purchase_orders_items(purchase_orders_id);
CREATE INDEX idx_poi_product ON purchase_orders_items(product_id);

