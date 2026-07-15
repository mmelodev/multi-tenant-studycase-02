package br.com.multi_tenant_studycase.config;

import br.com.multi_tenant_studycase.entities.User;
import br.com.multi_tenant_studycase.entities.UserRole;
import br.com.multi_tenant_studycase.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.platform-admin.username:platform_admin}")
    private String username;

    @Value("${app.platform-admin.email:platform-admin@system.local}")
    private String email;

    @Value("${app.platform-admin.password:ChangeMe@123}")
    private String password;

    @Value("${app.platform-admin.first-name:Platform}")
    private String firstName;

    @Value("${app.platform-admin.last-name:Admin}")
    private String lastName;

    @Override
    public void run(final String... args) {
        if (this.userRepository.existsByUsername(this.username)) {
            log.info("Platform admin '{}' already exists — skipping seed.", this.username);
            return;
        }

        if ("ChangeMe@123".equals(this.password)) {
            log.warn("Using the DEFAULT platform admin password. Set app.platform-admin.password "
                    + "(env APP_PLATFORM_ADMIN_PASSWORD) and change it in any real environment.");
        }

        final User admin = User.builder()
                .username(this.username)
                .email(this.email)
                .password(this.passwordEncoder.encode(this.password))
                .firstName(this.firstName)
                .lastName(this.lastName)
                .role(UserRole.ROLE_PLATFORM_ADMIN)
                .enabled(true)
                .tenant(null)          // admin da plataforma não pertence a nenhum tenant
                .deleted(false)
                .createdBy("system")   // sem usuário autenticado no boot -> AuditorAware devolve vazio
                .build();

        this.userRepository.save(admin);
        log.info("Platform admin '{}' created successfully.", this.username);
    }
}
