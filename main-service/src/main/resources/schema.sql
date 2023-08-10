drop table if exists users cascade;
CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name VARCHAR(250) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE
);

drop table if exists categories cascade;
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

drop table if exists events cascade;
CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories (id) ON UPDATE cascade ON DELETE restrict,
    created_on TIMESTAMP NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL REFERENCES users (id) ON UPDATE cascade ON DELETE restrict,
    location VARCHAR(20) NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INT NOT NULL,
    published_on TIMESTAMP,
    request_moderation BOOLEAN NOT NULL,
    state VARCHAR(20) NOT NULL,
    title VARCHAR(120) NOT NULL
);

drop table if exists participation_requests cascade;
CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created TIMESTAMP NOT NULL,
    event_id BIGINT NOT NULL REFERENCES events (id) ON UPDATE cascade ON DELETE cascade,
    requester_id BIGINT NOT NULL REFERENCES users (id) ON UPDATE cascade ON DELETE cascade,
    status VARCHAR(20) NOT NULL
);

drop table if exists compilations cascade;
CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(50) NOT NULL UNIQUE
);

drop table if exists event_compilation cascade;
CREATE TABLE IF NOT EXISTS event_compilation (
    e_id INTEGER REFERENCES events (id) ON UPDATE cascade ON DELETE cascade,
    c_id INTEGER REFERENCES compilations (id) ON UPDATE cascade ON DELETE cascade,
    CONSTRAINT event_compilation_pk PRIMARY KEY (e_id, c_id)
);