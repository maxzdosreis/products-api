ALTER TABLE purchase_orders_items
    MODIFY purchase_order_id BIGINT NOT NULL;

ALTER TABLE purchase_orders_items RENAME TO purchase_order_items;