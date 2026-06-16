package com.valledelsol.auth.initializer;

import com.valledelsol.auth.domain.Role;
import com.valledelsol.auth.domain.User;
import com.valledelsol.auth.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@test.com") == null) {
                var admin = new User(
                        "Admin",
                        "admin@test.com",
                        passwordEncoder.encode("123456")
                );
                admin.actualizarRol(Role.ADMIN);
                userRepository.save(admin);
                System.out.println(">>> Usuario admin creado");
            }
        };
    }
}
