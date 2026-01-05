package com.autenticacion.demo.config;

import com.autenticacion.demo.entity.Usuario;
import com.autenticacion.demo.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Crear Usuario Admin si no existe
            if (usuarioRepository.findByEmail("admin@example.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setRol("ADMIN");
                admin.setActivo(true);
                usuarioRepository.save(admin);
                System.out.println("Usuario admin creado: admin@example.com / password");
            }

            // Crear Usuario USER si no existe
            if (usuarioRepository.findByEmail("user@example.com").isEmpty()) {
                Usuario user = new Usuario();
                user.setUsername("user");
                user.setEmail("user@example.com");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRol("USER");
                user.setActivo(true);
                usuarioRepository.save(user);
                System.out.println("Usuario user creado: user@example.com / password");
            }
        };
    }
}
