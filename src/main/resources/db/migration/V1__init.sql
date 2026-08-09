-- Smart Task Management System — Production Schema (PostgreSQL)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- USERS
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    avatar_url      VARCHAR(500),
    role            user_role NOT NULL DEFAULT 'USER',
    status          user_status NOT NULL DEFAULT 'ACTIVE',
    timezone        VARCHAR(50) NOT NULL DEFAULT 'UTC',
    preferences     JSONB NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One user per email for life: deactivating an account (status = INACTIVE)
-- does not free the email up, it just makes the account recoverable via
-- that same email instead of registerable as a brand-new account.
CREATE UNIQUE INDEX uq_users_email ON users (email);

-- CATEGORIES
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(50) NOT NULL,
    icon        VARCHAR(50),
    color       VARCHAR(7),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_categories_user_name ON categories (user_id, lower(name));
CREATE INDEX idx_categories_user ON categories (user_id);

-- TAGS
CREATE TABLE tags (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(30) NOT NULL,
    color       VARCHAR(7),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_tags_user_name ON tags (user_id, lower(name));

-- TASKS (core table)
CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

CREATE TABLE tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     UUID REFERENCES categories(id) ON DELETE SET NULL,

    title           VARCHAR(255) NOT NULL,
    description     TEXT,

    status          task_status NOT NULL DEFAULT 'PENDING',
    priority        task_priority NOT NULL DEFAULT 'MEDIUM',

    start_date      TIMESTAMPTZ,
    due_date        TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,

    estimated_minutes INT,
    actual_minutes    INT,

    is_archived     BOOLEAN NOT NULL DEFAULT false,

    ai_metadata     JSONB NOT NULL DEFAULT '{}',

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT chk_dates CHECK (due_date IS NULL OR start_date IS NULL OR due_date >= start_date)
);

CREATE INDEX idx_tasks_user_status ON tasks (user_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_due_date ON tasks (user_id, due_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_priority ON tasks (user_id, priority) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_archived ON tasks (user_id, is_archived) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_title_trgm ON tasks USING gin (title gin_trgm_ops);

-- SUBTASKS (lightweight checklist items — NOT full tasks)
-- No status ENUM, priority, tags, or attachments by design: these
-- are meant to be simple checkbox items rendered inline under a
-- parent task (e.g. "Buy groceries" -> "milk", "eggs", "bread").
CREATE TABLE subtasks (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id       UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title         VARCHAR(255) NOT NULL,
    is_completed  BOOLEAN NOT NULL DEFAULT false,
    position      INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_subtasks_task ON subtasks (task_id);

-- TASK_TAGS (many-to-many)
CREATE TABLE task_tags (
    task_id  UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag_id   UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, tag_id)
);

CREATE INDEX idx_task_tags_tag ON task_tags (tag_id);

-- ATTACHMENTS
CREATE TABLE attachments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id      UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- denormalized for fast ownership checks
    file_name    VARCHAR(255) NOT NULL,
    file_url     VARCHAR(500) NOT NULL, -- S3 / Cloudinary URL, not the blob itself
    mime_type    VARCHAR(100) NOT NULL,
    file_size_kb INT NOT NULL,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachments_task ON attachments (task_id);
CREATE INDEX idx_attachments_user ON attachments (user_id);

-- RECURRING PATTERNS
CREATE TYPE recurrence_frequency AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY');

CREATE TABLE recurring_patterns (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id        UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    frequency      recurrence_frequency NOT NULL,
    interval_count INT NOT NULL DEFAULT 1,
    days_of_week   JSONB, -- e.g. [1,3,5] for Mon/Wed/Fri
    end_date       TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_recurring_task ON recurring_patterns (task_id);

-- REMINDERS (distinct from due_date — a task can have several)
CREATE TABLE reminders (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id       UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    remind_at     TIMESTAMPTZ NOT NULL,
    channel       VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    sent_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reminders_due ON reminders (remind_at) WHERE sent_at IS NULL;

-- AI SUGGESTIONS (trackable, actionable AI output — not just a JSON blob)
CREATE TYPE suggestion_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE suggestion_type AS ENUM ('SUBTASK', 'PRIORITY_CHANGE', 'DUE_DATE', 'SUMMARY', 'CATEGORY');

CREATE TABLE ai_suggestions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id      UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    type         suggestion_type NOT NULL,
    payload      JSONB NOT NULL, -- shape depends on `type`
    status       suggestion_status NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at  TIMESTAMPTZ
);

CREATE INDEX idx_ai_suggestions_task ON ai_suggestions (task_id, status);
