ALTER TABLE url_list
    ADD page_rank DOUBLE NOT NULL DEFAULT 1;

UPDATE url_list
SET page_rank = 1;