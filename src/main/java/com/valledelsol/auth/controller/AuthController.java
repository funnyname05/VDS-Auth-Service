package com.valledelsol.auth.controller;

import com.valledelsol.auth.domain.User;
import com.valledelsol.auth.domain.UserRepository;
import com.valledelsol.auth.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Migrado desde ValleDelSol.demo.controller.AuthController.
 * <p>
 * NUEVO: endpoint GET /auth/validate que el BFF (y otros servicios)
 * llaman para verificar un JWT sin tener que replicar la lógica.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    // ── Login ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest datos) {
        var authToken = new UsernamePasswordAuthenticationToken(datos.email(), datos.password());
        var auth = authenticationManager.authenticate(authToken);
        var jwt = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new TokenResponse(jwt));
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/registro")
    public ResponseEntity<String> registro(@RequestBody RegistroRequest datos) {
        if (userRepository.findByEmail(datos.email()) != null) {
            return ResponseEntity.badRequest().body("Email ya registrado");
        }
        var user = new User(
                datos.nombre(),
                datos.email(),
                passwordEncoder.encode(datos.password())
        );
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado");
    }

    public record RegistroRequest(String nombre, String email, String password) {}

    // ── Records internos (DTOs simples) ───────────────────────────────────────
    public record LoginRequest(String email, String password) {
    }

    public record TokenResponse(String token) {
    }

    public record ValidateResponse(String email, String role) {
    }
}
