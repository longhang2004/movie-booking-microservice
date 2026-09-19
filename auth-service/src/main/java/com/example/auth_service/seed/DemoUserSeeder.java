package com.example.auth_service.seed;

import com.example.auth_service.model.Role;
import com.example.auth_service.model.UserAccount;
import com.example.auth_service.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DemoUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("admin@cinema.local", "Admin@123", "Cinema Admin", Role.ADMIN);
        seed("user@cinema.local", "User@123", "Demo Customer", Role.USER);
    }

    private void seed(String email, String rawPassword, String fullName, Role role) {
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(true);
        userAccountRepository.save(user);
        log.info("Seeded {} account {}", role, email);
    }
}
