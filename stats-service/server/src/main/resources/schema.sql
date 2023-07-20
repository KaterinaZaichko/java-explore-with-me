drop table if exists hits cascade;
CREATE TABLE IF NOT EXISTS hits (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  app VARCHAR(100) NOT NULL,
  uri VARCHAR(100),
  ip VARCHAR(15) NOT NULL,
  time_stamp VARCHAR(19) NOT NULL
);