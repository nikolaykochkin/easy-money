DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR        NOT NULL,
    login              VARCHAR UNIQUE NOT NULL,
    password           VARCHAR,
    telegram_id        BIGINT UNIQUE,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

INSERT INTO users (name, login, password, telegram_id, created_date, last_modified_date)
VALUES ('Nikolai', 'nikolai', 'P@ssw0rd', 74543363, NOW(), NOW());

DROP TABLE IF EXISTS telegram_message CASCADE;

CREATE TABLE IF NOT EXISTS telegram_message
(
    id                 BIGSERIAL PRIMARY KEY,
    uuid               UUID UNIQUE NOT NULL,
    message_date       TIMESTAMP   NOT NULL,
    update_id          INTEGER     NOT NULL,
    message_id         INTEGER     NOT NULL,
    chat_id            BIGINT      NOT NULL,
    tg_user_id         BIGINT      NOT NULL,
    voice_file_path    VARCHAR,
    photo_file_path    VARCHAR,
    text               TEXT,
    update             JSONB       NOT NULL,
    file_metadata      JSONB,
    user_id            BIGINT REFERENCES users (id),
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

DROP TABLE IF EXISTS command CASCADE;

CREATE TABLE IF NOT EXISTS command
(
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID UNIQUE                   NOT NULL,
    source              SMALLINT                      NOT NULL,
    state               SMALLINT                      NOT NULL,
    type                SMALLINT                      NOT NULL,
    content             TEXT,
    error               TEXT,
    sql                 TEXT,
    telegram_message_id BIGINT REFERENCES telegram_message (id),
    user_id             BIGINT REFERENCES users (id) NOT NULL,
    created_date        TIMESTAMP,
    last_modified_date  TIMESTAMP
);

DROP TABLE IF EXISTS command_attachment CASCADE;

CREATE TABLE IF NOT EXISTS command_attachment
(
    id          BIGSERIAL PRIMARY KEY,
    uuid        UUID                           NOT NULL,
    command_id  BIGINT REFERENCES command (id) NOT NULL,
    url         VARCHAR                        NOT NULL,
    telegram_id VARCHAR,
    storage_id  VARCHAR,
    filename    VARCHAR,
    filesize    BIGINT,
    mime_type   VARCHAR
);

CREATE INDEX command_attachment_uuid_index ON command_attachment (uuid);

DROP TABLE IF EXISTS category CASCADE;

CREATE TABLE IF NOT EXISTS category
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR NOT NULL,
    category_group     VARCHAR,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

DROP TABLE IF EXISTS account CASCADE;

CREATE TABLE IF NOT EXISTS account
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR                       NOT NULL,
    account_group      VARCHAR,
    account_type       SMALLINT                      NOT NULL,
    currency           VARCHAR                       NOT NULL,
    owner_id           BIGINT REFERENCES users (id) NOT NULL,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

DROP TABLE IF EXISTS counterparty CASCADE;

CREATE TABLE IF NOT EXISTS counterparty
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR NOT NULL,
    counterparty_group VARCHAR,
    external_id        VARCHAR,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

DROP TABLE IF EXISTS invoice CASCADE;

CREATE TABLE IF NOT EXISTS invoice
(
    id                 BIGSERIAL PRIMARY KEY,
    uuid               UUID                          NOT NULL,
    date_time          TIMESTAMP                     NOT NULL,
    external_id        VARCHAR,
    url                VARCHAR,
    content            TEXT,
    currency           VARCHAR                       NOT NULL,
    seller_id          BIGINT REFERENCES counterparty (id),
    user_id            BIGINT REFERENCES users (id) NOT NULL,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

DROP TABLE IF EXISTS item CASCADE;

CREATE TABLE IF NOT EXISTS item
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR NOT NULL,
    item_group         VARCHAR,
    category_id        BIGINT REFERENCES category (id),
    external_id        VARCHAR,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

DROP TABLE IF EXISTS invoice_item CASCADE;

CREATE TABLE IF NOT EXISTS invoice_item
(
    id         BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT REFERENCES invoice (id) NOT NULL,
    item_id    BIGINT REFERENCES item (id)    NOT NULL,
    quantity   REAL                           NOT NULL,
    unit_price NUMERIC(14, 2)                 NOT NULL,
    price      NUMERIC(14, 2)                 NOT NULL
);

DROP TABLE IF EXISTS transaction CASCADE;

CREATE TABLE IF NOT EXISTS transaction
(
    id                 BIGSERIAL PRIMARY KEY,
    uuid               UUID                           NOT NULL,
    timestamp          TIMESTAMP                      NOT NULL,
    draft              BOOLEAN                        NOT NULL,
    transaction_type   SMALLINT                       NOT NULL,
    account_id         BIGINT REFERENCES account (id) NOT NULL,
    category_id        BIGINT REFERENCES category (id),
    counterparty_id    BIGINT REFERENCES counterparty (id),
    invoice_id         BIGINT REFERENCES invoice (id),
    currency           VARCHAR                        NOT NULL,
    sum                NUMERIC(14, 2)                 NOT NULL,
    comment            VARCHAR(200)                   NOT NULL,
    user_id            BIGINT REFERENCES users (id)  NOT NULL,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);
