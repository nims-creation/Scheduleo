-- =============================================================================
-- V1__init.sql — Schedulo Full Database Schema
-- Purpose  : Documents the complete schema used in production (Supabase).
--            Flyway is kept disabled in prod (Supabase manages DDL),
--            but this file is used by the test profile to validate integrity.
-- =============================================================================

-- ── Extensions ───────────────────────────────────────────────────────────────
-- pgcrypto is available on Supabase; gen_random_uuid() used for PK generation
-- Note: H2 test profile uses its own UUID strategy — this line is skipped in H2

-- ── Roles ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS roles (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN      NOT NULL DEFAULT true,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    version        BIGINT,
    name           VARCHAR(50)  NOT NULL UNIQUE,
    description    TEXT,
    is_system_role BOOLEAN      NOT NULL DEFAULT false
);

-- ── Permissions ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS permissions (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    is_deleted BOOLEAN      NOT NULL DEFAULT false,
    version    BIGINT,
    resource   VARCHAR(100) NOT NULL,
    action     VARCHAR(50)  NOT NULL,
    description TEXT
);

-- ── Role ↔ Permission (join table) ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ── Organizations ─────────────────────────────────────────────────────────────
-- NOTE: owner_id FK added after users table creation (see ALTER at bottom)
CREATE TABLE IF NOT EXISTS organizations (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at             TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP,
    created_by             VARCHAR(255),
    updated_by             VARCHAR(255),
    is_active              BOOLEAN       NOT NULL DEFAULT true,
    is_deleted             BOOLEAN       NOT NULL DEFAULT false,
    version                BIGINT,
    name                   VARCHAR(255)  NOT NULL,
    slug                   VARCHAR(100)  NOT NULL UNIQUE,
    description            TEXT,
    organization_type      VARCHAR(50)   NOT NULL,
    logo_url               TEXT,
    website                TEXT,
    email                  VARCHAR(255),
    phone                  VARCHAR(20),
    street_address         TEXT,
    city                   VARCHAR(100),
    state                  VARCHAR(100),
    postal_code            VARCHAR(20),
    country                VARCHAR(100),
    timezone               VARCHAR(50)   DEFAULT 'UTC',
    working_days           VARCHAR(100)  DEFAULT 'MON,TUE,WED,THU,FRI',
    working_hours_start    VARCHAR(10)   DEFAULT '08:00',
    working_hours_end      VARCHAR(10)   DEFAULT '18:00',
    slot_duration_minutes  INTEGER       DEFAULT 60,
    max_users              INTEGER       DEFAULT 10,
    max_schedules_per_day  INTEGER       DEFAULT 100,
    settings               JSONB         DEFAULT '{}',
    owner_id               UUID          -- FK added below after users table
);
CREATE INDEX IF NOT EXISTS idx_org_slug ON organizations(slug);
CREATE INDEX IF NOT EXISTS idx_org_type ON organizations(organization_type);

-- ── Users ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at                  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR(255),
    updated_by                  VARCHAR(255),
    is_active                   BOOLEAN      NOT NULL DEFAULT true,
    is_deleted                  BOOLEAN      NOT NULL DEFAULT false,
    version                     BIGINT,
    email                       VARCHAR(255) NOT NULL UNIQUE,
    password_hash               TEXT         NOT NULL,
    first_name                  VARCHAR(100) NOT NULL,
    last_name                   VARCHAR(100) NOT NULL,
    phone_number                VARCHAR(20),
    profile_image_url           TEXT,
    timezone                    VARCHAR(50)  DEFAULT 'UTC',
    locale                      VARCHAR(10)  DEFAULT 'en',
    auth_provider               VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id                 TEXT,
    email_verified              BOOLEAN      NOT NULL DEFAULT false,
    email_verification_token    TEXT,
    password_reset_token        TEXT,
    password_reset_token_expiry TIMESTAMP,
    last_login_at               TIMESTAMP,
    failed_login_attempts       INTEGER      DEFAULT 0,
    account_locked_until        TIMESTAMP,
    organization_id             UUID         REFERENCES organizations(id) ON DELETE SET NULL,
    two_factor_enabled          BOOLEAN      NOT NULL DEFAULT false,
    two_factor_secret           TEXT
);
CREATE INDEX IF NOT EXISTS idx_user_email        ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_organization ON users(organization_id);

-- Now that users table exists, add the owner_id FK on organizations
ALTER TABLE organizations
    ADD CONSTRAINT fk_org_owner FOREIGN KEY (owner_id) REFERENCES users(id);

-- ── User ↔ Role (join table) ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ── Refresh Tokens ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    is_active   BOOLEAN   NOT NULL DEFAULT true,
    is_deleted  BOOLEAN   NOT NULL DEFAULT false,
    version     BIGINT,
    user_id     UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       TEXT      NOT NULL UNIQUE,
    expiry_date TIMESTAMPTZ NOT NULL,
    device_info TEXT,
    ip_address  VARCHAR(45),
    is_revoked  BOOLEAN   NOT NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);

-- ── Departments ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    version         BIGINT,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    organization_id UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE
);

-- ── Branches ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS branches (
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    version         BIGINT,
    name            VARCHAR(255) NOT NULL,
    organization_id UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE
);

-- ── Resources (rooms, labs, equipment) ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS resources (
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    version         BIGINT,
    name            VARCHAR(255) NOT NULL,
    resource_type   VARCHAR(50)  NOT NULL,
    capacity        INTEGER,
    location        TEXT,
    description     TEXT,
    organization_id UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE
);

-- ── Subscriptions ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS subscriptions (
    id                UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    is_active         BOOLEAN      NOT NULL DEFAULT true,
    is_deleted        BOOLEAN      NOT NULL DEFAULT false,
    version           BIGINT,
    organization_id   UUID         NOT NULL UNIQUE REFERENCES organizations(id),
    plan              VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    status            VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    current_period_start TIMESTAMP,
    current_period_end   TIMESTAMP,
    stripe_customer_id   TEXT,
    stripe_subscription_id TEXT,
    cancel_at_period_end BOOLEAN   DEFAULT false
);

-- ── Timetables ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS timetables (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at         TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),
    is_active          BOOLEAN      NOT NULL DEFAULT true,
    is_deleted         BOOLEAN      NOT NULL DEFAULT false,
    version            BIGINT,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    organization_id    UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    department_id      UUID         REFERENCES departments(id) ON DELETE SET NULL,
    effective_from     DATE         NOT NULL,
    effective_to       DATE,
    status             VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    timetable_type     VARCHAR(50)  NOT NULL,
    is_template        BOOLEAN      NOT NULL DEFAULT false,
    template_name      TEXT,
    recurrence_pattern TEXT
);
CREATE INDEX IF NOT EXISTS idx_timetable_org   ON timetables(organization_id);
CREATE INDEX IF NOT EXISTS idx_timetable_dates ON timetables(effective_from, effective_to);

-- ── Time Slots ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS time_slots (
    id           UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    is_active    BOOLEAN   NOT NULL DEFAULT true,
    is_deleted   BOOLEAN   NOT NULL DEFAULT false,
    version      BIGINT,
    timetable_id UUID      NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,
    name         VARCHAR(100),
    start_time   TIME      NOT NULL,
    end_time     TIME      NOT NULL,
    day_of_week  VARCHAR(20),
    slot_order   INTEGER
);

-- ── Recurring Patterns ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS recurring_patterns (
    id             UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN   NOT NULL DEFAULT true,
    is_deleted     BOOLEAN   NOT NULL DEFAULT false,
    version        BIGINT,
    frequency      VARCHAR(20) NOT NULL,
    interval_count INTEGER     DEFAULT 1,
    end_date       DATE,
    days_of_week   VARCHAR(100)
);

-- ── Schedule Entries ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS schedule_entries (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    is_active           BOOLEAN      NOT NULL DEFAULT true,
    is_deleted          BOOLEAN      NOT NULL DEFAULT false,
    version             BIGINT,
    timetable_id        UUID         NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,
    time_slot_id        UUID         REFERENCES time_slots(id) ON DELETE SET NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    day_of_week         VARCHAR(20),
    schedule_date       DATE,
    start_datetime      TIMESTAMP,
    end_datetime        TIMESTAMP,
    resource_id         UUID         REFERENCES resources(id) ON DELETE SET NULL,
    assigned_to_id      UUID         REFERENCES users(id) ON DELETE SET NULL,
    entry_type          VARCHAR(50)  NOT NULL,
    status              VARCHAR(50)  NOT NULL DEFAULT 'SCHEDULED',
    color               VARCHAR(7),
    is_recurring        BOOLEAN      NOT NULL DEFAULT false,
    recurring_pattern_id UUID        REFERENCES recurring_patterns(id) ON DELETE SET NULL,
    notes               TEXT,
    metadata            TEXT
);
CREATE INDEX IF NOT EXISTS idx_schedule_timetable ON schedule_entries(timetable_id);
CREATE INDEX IF NOT EXISTS idx_schedule_date      ON schedule_entries(schedule_date);
CREATE INDEX IF NOT EXISTS idx_schedule_resource  ON schedule_entries(resource_id);
CREATE INDEX IF NOT EXISTS idx_schedule_assigned  ON schedule_entries(assigned_to_id);

-- ── Schedule Entry ↔ Participants (join table) ────────────────────────────────
CREATE TABLE IF NOT EXISTS schedule_entry_participants (
    schedule_entry_id UUID NOT NULL REFERENCES schedule_entries(id) ON DELETE CASCADE,
    user_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (schedule_entry_id, user_id)
);

-- ── Audit Log ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN      NOT NULL DEFAULT true,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    version        BIGINT,
    organization_id UUID        REFERENCES organizations(id) ON DELETE CASCADE,
    user_id        UUID         REFERENCES users(id) ON DELETE SET NULL,
    action         VARCHAR(50)  NOT NULL,
    entity_type    VARCHAR(100),
    entity_id      TEXT,
    old_values     TEXT,
    new_values     TEXT,
    ip_address     VARCHAR(45),
    user_agent     TEXT
);

-- ── Seed: System Roles ────────────────────────────────────────────────────────
INSERT INTO roles (id, name, description, is_system_role, is_active, is_deleted, version)
VALUES
    (gen_random_uuid(), 'ROLE_ADMIN',  'Administrator with full access',  true, true, false, 0),
    (gen_random_uuid(), 'ROLE_USER',   'Standard authenticated user',     true, true, false, 0),
    (gen_random_uuid(), 'ROLE_MEMBER', 'Organisation member with limited access', true, true, false, 0)
ON CONFLICT (name) DO NOTHING;
