package com.resolvehub.app;

import com.resolvehub.auth.domain.RoleEntity;
import com.resolvehub.auth.domain.UserEntity;
import com.resolvehub.auth.domain.UserStatus;
import com.resolvehub.auth.repository.RoleRepository;
import com.resolvehub.auth.repository.UserRepository;
import com.resolvehub.common.security.RoleNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedAdminPassword;

    public DataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${resolvehub.seed.admin-password:Admin123!}") String seedAdminPassword
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedAdminPassword = seedAdminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureRole(RoleNames.USER);
        ensureRole(RoleNames.MODERATOR);
        ensureRole(RoleNames.ADMIN);
        ensureAdminUser();
    }

    private void ensureRole(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new RoleEntity(roleName));
            log.info("Seeded role {}", roleName);
        }
    }

    private void ensureAdminUser() {
        if (userRepository.existsByEmailIgnoreCase("admin@resolvehub.local")) {
            return;
        }
        UserEntity admin = new UserEntity();
        admin.setEmail("admin@resolvehub.local");
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode(seedAdminPassword));
        admin.setStatus(UserStatus.ACTIVE);
        admin.getRoles().addAll(List.of(
                roleRepository.findByName(RoleNames.USER).orElseThrow(),
                roleRepository.findByName(RoleNames.MODERATOR).orElseThrow(),
                roleRepository.findByName(RoleNames.ADMIN).orElseThrow()
        ));
        userRepository.save(admin);
        log.info("Seeded admin user admin@resolvehub.local");
    }
}
