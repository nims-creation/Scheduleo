package com.saas.Schedulo.config;

import com.saas.Schedulo.entity.user.Role;
import com.saas.Schedulo.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            log.info("Seeding database with default roles...");

            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Standard User Role")
                    .isSystemRole(true)
                    .build();

            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator Role")
                    .isSystemRole(true)
                    .build();

            roleRepository.save(userRole);
            roleRepository.save(adminRole);

            log.info("Successfully seeded database with {} roles.", roleRepository.count());
        } else {
            log.info("Database already contains roles. Skipping seeder.");
        }
    }
}
