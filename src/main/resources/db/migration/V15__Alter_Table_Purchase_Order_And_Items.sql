ALTER TABLE purchase_orders
    MODIFY confirmed_at DATETIME NULL;

ALTER TABLE purchase_orders
    MODIFY received_at DATETIME NULL;

ALTER TABLE purchase_orders_items
    CHANGE purchase_orders_id purchase_order_id BIGINT;

ALTER TABLE purchase_orders_items
    ADD COLUMN received_quantity DECIMAL(19,3) NOT NULL DEFAULT 0.000;