-- Drop the table if it exists to ensure a clean state
DROP TABLE IF EXISTS products;

-- Create the 'products' table
CREATE TABLE products
(
    id         SERIAL,
    version    INTEGER NOT NULL DEFAULT 0,
    product_id INTEGER NOT NULL,
    name       VARCHAR(255),
    weight     INTEGER NOT NULL,
    PRIMARY KEY (id)
);

-- Create an index for the 'product_id' column
CREATE INDEX products_product_id_idx ON products (product_id);

-- Create a unique constraint on product_id to ensure uniqueness
ALTER TABLE products
    ADD CONSTRAINT products_product_id_unique UNIQUE (product_id);