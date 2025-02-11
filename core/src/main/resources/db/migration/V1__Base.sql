CREATE TABLE todo (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  schedule TEXT NOT NULL,
  notify_before TEXT NOT NULL,
  last_run INTEGER,
  created_at INTEGER NOT NULL
);

CREATE TABLE weather_measurement (
  collected_at INTEGER NOT NULL,
  temperature DOUBLE NOT NULL,
  source TEXT NOT NULL
);

CREATE TABLE plant (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    created_at INTEGER NOT NULL
);

CREATE TABLE user (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at INTEGER NOT NULL
)