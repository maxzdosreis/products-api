CREATE TABLE IF NOT EXISTS categories (
    id_category BIGINT NOT NULL auto_increment,
    name_category VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_category),
    CONSTRAINT uk_category_name UNIQUE (name_category)
);

CREATE INDEX idx_category_name ON categories(name_category);

-- Categoria padrão para produtos já existentes
INSERT INTO categories (name_category, description, enabled)
VALUES ('Geral','Categoria padrão para produtos sem classificação', TRUE);