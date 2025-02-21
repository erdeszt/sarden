CREATE TABLE user (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at INTEGER NOT NULL
);

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

CREATE TABLE variety (
    id TEXT NOT NULL PRIMARY KEY,
    plant_id TEXT NOT NULL,
    name TEXT NOT NULL UNIQUE,
    created_at INTEGER NOT NULL
);

CREATE TABLE companion (
    id TEXT NOT NULL PRIMARY KEY,
    companion_plant_id TEXT NOT NULL,
    target_plant_id TEXT NOT NULL,
    benefits TEXT NOT NULL,
    created_at INTEGER NOT NULL
);

CREATE TABLE sowlog (
    id TEXT NOT NULL PRIMARY KEY,
    plant_id TEXT NOT NULL,
    sowing_date TEXT NOT NULL,
    details TEXT NOT NULL,
    created_at INTEGER NOT NULL
)