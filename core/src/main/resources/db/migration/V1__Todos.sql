CREATE TABLE todo (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  schedule TEXT NOT NULL,
  notify_before TEXT NOT NULL,
  last_run INTEGER,
  created_at INTEGER NOT NULL
)