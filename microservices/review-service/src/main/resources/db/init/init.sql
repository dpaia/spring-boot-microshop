-- Create a database if it doesn't exist
CREATE DATABASE IF NOT EXISTS `review-db`;

-- Use the review-db database
USE `review-db`;

-- Drop the tables if they exist to ensure a clean state
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS reviews_seq;

-- Create the 'reviews' table
CREATE TABLE reviews
(
    id         INT NOT NULL AUTO_INCREMENT,
    version    INT NOT NULL DEFAULT 0,
    product_id INT NOT NULL,
    review_id  INT NOT NULL,
    author     VARCHAR(255),
    subject    VARCHAR(255),
    content    TEXT,
    PRIMARY KEY (id),
    UNIQUE INDEX reviews_unique_idx (product_id, review_id)
);

-- Create the sequence table for Hibernate
CREATE TABLE reviews_seq
(
    next_val BIGINT NOT NULL,
    PRIMARY KEY (next_val)
);

-- Initialize the sequence with a starting value
INSERT INTO reviews_seq
VALUES (1);