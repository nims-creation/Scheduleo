package com.saas.Schedulo.config;

import com.saas.Schedulo.entity.user.Role;
import com.saas.Schedulo.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Wrap entirely — if DB is unreachable at startup the app still boots.
        // Health check will surface the real connection state.
        try {
            tryAlterResourcesTable();
            seedRoles();
        } catch (Exception e) {
            log.error("DatabaseSeeder failed (DB may be unreachable): {}", e.getMessage());
        }
    }

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
                log.debug("ALTER skipped (column may not exist): {}", e.getMessage());
            }
        }
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) {
            log.info("Roles already exist — skipping seed.");
            return;
        }
        log.info("Seeding default roles...");
        roleRepository.save(Role.builder().name("ROLE_USER").description("Standard User Role").isSystemRole(true).build());
        roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Administrator Role").isSystemRole(true).build());
        roleRepository.save(Role.builder().name("ROLE_MEMBER").description("Organisation Member").isSystemRole(true).build());
        log.info("Seeded {} roles.", roleRepository.count());
    }
}
