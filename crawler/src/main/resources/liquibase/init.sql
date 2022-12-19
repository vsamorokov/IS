create table word_list
(
    id       SERIAL PRIMARY KEY,
    word     varchar(512) not null,
    filtered boolean
);

CREATE UNIQUE INDEX word_unique_idx ON word_list (word);

create table url_list
(
    id      SERIAL PRIMARY KEY,
    url     varchar(512) not null,
    crawled boolean
);

CREATE UNIQUE INDEX url_unique_idx ON url_list (url);

create table word_location
(
    id       SERIAL PRIMARY KEY,
    word_id  bigint not null references word_list (id),
    url_id   bigint not null references url_list (id),
    location int not null
);

CREATE UNIQUE INDEX word_url_location_unique_idx ON word_location (word_id, url_id, location);

create table link_between_url
(
    id          SERIAL PRIMARY KEY,
    from_url_id bigint not null references url_list (id),
    to_url_id   bigint not null references url_list (id)
);

CREATE UNIQUE INDEX from_to_unique_idx ON link_between_url (from_url_id, to_url_id);

create table link_word
(
    id      SERIAL PRIMARY KEY,
    word_id bigint not null references word_list (id),
    link_id bigint not null references link_between_url (id)
);

CREATE UNIQUE INDEX word_link_unique_idx ON link_word (word_id, link_id);
