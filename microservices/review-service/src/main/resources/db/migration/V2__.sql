ALTER TABLE reviews
    ADD date date NULL;

ALTER TABLE reviews
    MODIFY date date NOT NULL;