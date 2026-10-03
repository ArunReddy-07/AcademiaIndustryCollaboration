package com.academiaindustry.config;

import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.entity.Role;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class DevelopmentAdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final String name;
    private final String email;
    private final String password;

    public DevelopmentAdminBootstrap(UserRepository userRepository,
                                     UserService userService,
                                     @Value("${app.bootstrap-admin.name:Portal Administrator}") String name,
                                     @Value("${app.bootstrap-admin.email:}") String email,
                                     @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password) || password.length() < 12) {
            throw new IllegalStateException(
                    "Development Admin bootstrap requires ADMIN_EMAIL and an ADMIN_PASSWORD of at least 12 characters.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        UserRequest request = new UserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setRole(Role.ADMIN);
        userService.create(request);
    }
}
