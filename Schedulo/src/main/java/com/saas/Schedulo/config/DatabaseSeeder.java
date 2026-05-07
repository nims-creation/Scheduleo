package com.saas.Schedulo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seeds essential reference data (roles) using raw JDBC so it is completely
 * independent of the JPA transaction manager. This avoids
 * CannotCreateTransactionException on cold-start when pgBouncer has not yet
 * established a warm connection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        retryOnFailure(this::tryAlterResourcesTable, "ALTER resources table");
        retryOnFailure(this::seedRoles,               "seed roles");
    }

    // ── Retry wrapper ─────────────────────────────────────────────────────────

    private void retryOnFailure(Runnable task, String label) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                task.run();
                return;
            } catch (Exception e) {
                log.warn("DatabaseSeeder [{}] attempt {}/{} failed: {}",
                        label, attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("DatabaseSeeder [{}] gave up after {} attempts — "
                            + "app is running but may need manual role seeding.", label, maxAttempts);
                }
            }
        }
    }

    // ── Schema adjustments ────────────────────────────────────────────────────

    private void tryAlterResourcesTable() {
        String[] statements = {
            "ALTER TABLE resources ALTER COLUMN resource_type DROP NOT NULL",
            "ALTER TABLE resources ALTER COLUMN type DROP NOT NULL",
            "ALTER TABLE resources ALTER COLUMN is_bookable DROP NOT NULL",
            "ALTER TABLE resources ALTER COLUMN requires_approval DROP NOT NULL"
        };
        for (String sql : statements) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.debug("ALTER skipped (column may not exist or already nullable): {}",
                        e.getMessage());
            }
        }
    }

    // ── Role seeding via plain JDBC (no JPA / no @Transactional needed) ───────

    private void seedRoles() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM roles", Integer.class);

        if (count != null && count > 0) {
            log.info("Roles already exist ({}) — skipping seed.", count);
            return;
        }

        log.info("Seeding default roles via JDBC...");

        // ON CONFLICT DO NOTHING is idempotent — safe to run multiple times
        String sql = "INSERT INTO roles "
                + "(id, name, description, is_system_role, is_active, is_deleted, version) "
                + "VALUES (?::uuid, ?, ?, true, true, false, 0) "
                + "ON CONFLICT (name) DO NOTHING";

        jdbcTemplate.update(sql, UUID.randomUUID().toString(),
                "ROLE_USER",   "Standard User Role");
        jdbcTemplate.update(sql, UUID.randomUUID().toString(),
                "ROLE_ADMIN",  "Administrator Role");
        jdbcTemplate.update(sql, UUID.randomUUID().toString(),
                "ROLE_MEMBER", "Organisation Member");

        log.info("Default roles seeded successfully.");
    }
}
