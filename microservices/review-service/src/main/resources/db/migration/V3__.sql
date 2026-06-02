ALTER TABLE reviews
    ADD rating INT NULL;

ALTER TABLE reviews
    MODIFY rating INT NOT NULL;