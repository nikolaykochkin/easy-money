CREATE SCHEMA IF NOT EXISTS stage;

DROP TABLE IF EXISTS stage.telegram_messages;

CREATE TABLE IF NOT EXISTS stage.telegram_messages
(
    id             BIGSERIAL PRIMARY KEY,
    uuid           UUID UNIQUE NOT NULL,
    update_id      INTEGER     NOT NULL,
    message_id     INTEGER     NOT NULL,
    chat_id        BIGINT      NOT NULL,
    user_id        BIGINT      NOT NULL,
    has_attachment BOOLEAN     NOT NULL,
    content        TEXT        NOT NULL
);

CREATE SCHEMA IF NOT EXISTS command;

DROP TABLE IF EXISTS command.commands CASCADE;

CREATE TABLE IF NOT EXISTS command.commands
(
    id      BIGSERIAL PRIMARY KEY,
    uuid    UUID UNIQUE NOT NULL,
    source  VARCHAR     NOT NULL,
    state   VARCHAR     NOT NULL,
    type    VARCHAR     NOT NULL,
    content TEXT,
    sql     TEXT
);

DROP TABLE IF EXISTS command.attachments CASCADE;

CREATE TABLE IF NOT EXISTS command.attachments
(
    id             BIGSERIAL PRIMARY KEY,
    uuid           UUID UNIQUE NOT NULL,
    url            VARCHAR     NOT NULL,
    storage_id     VARCHAR,
    storage_bucket VARCHAR,
    e_tag          VARCHAR,
    filename       VARCHAR,
    filesize       BIGINT,
    mime_type      VARCHAR
);