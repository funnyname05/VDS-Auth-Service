package com.valledelsol.auth.service;

import com.valledelsol.auth.domain.Role;
import com.valledelsol.auth.domain.User;
import com.valledelsol.auth.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenService — pruebas unitarias")
class TokenServiceTest {

    private TokenService tokenService;

    // Usuario de prueba reutilizable en todos los tests
    private User usuarioPrueba;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Inyectamos el secreto directamente sin necesitar Spring context
        ReflectionTestUtils.setField(tokenService, "apiSecret",
                "secreto-de-prueba-que-debe-ser-muy-largo-para-hmac256");

        usuarioPrueba = new User("Admin", "admin@test.com", "password123");
        usuarioPrueba.actualizarRol(Role.ADMIN);
        // Simulamos el ID que normalmente asigna la DB
        ReflectionTestUtils.setField(usuarioPrueba, "id", 1L);
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken debe retornar un token no nulo")
    void generateToken_debeRetornarTokenNoNulo() {
        var token = tokenService.generateToken(usuarioPrueba);
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken debe retornar un JWT con 3 partes separadas por punto")
    void generateToken_debeRetornarJwtValido() {
        var token = tokenService.generateToken(usuarioPrueba);
        var partes = token.split("\\.");
        assertThat(partes).hasSize(3);
    }

    @Test
    @DisplayName("getSubject debe retornar el email del usuario a partir de un token válido")
    void getSubject_conTokenValido_debeRetornarEmail() {
        var token = tokenService.generateToken(usuarioPrueba);
        var subject = tokenService.getSubject(token);
        assertThat(subject).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("getSubject con token inválido debe retornar null")
    void getSubject_conTokenInvalido_debeRetornarNull() {
        var subject = tokenService.getSubject("token.invalido.aqui");
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("getSubject con null debe retornar null sin lanzar excepción")
    void getSubject_conNull_debeRetornarNull() {
        var subject = tokenService.getSubject(null);
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("getSubject con token de otro secreto debe retornar null")
    void getSubject_conTokenDeOtroSecreto_debeRetornarNull() {
        // Creamos un TokenService con diferente secreto
        var otroService = new TokenService();
        ReflectionTestUtils.setField(otroService, "apiSecret", "otro-secreto-completamente-diferente");
        var tokenDeOtroServicio = otroService.generateToken(usuarioPrueba);

        // El tokenService original no debe reconocer ese token
        var subject = tokenService.getSubject(tokenDeOtroServicio);
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("generateToken con usuario CIVIL debe generar token válido")
    void generateToken_conUsuarioCivil_debeGenerarTokenValido() {
        var civil = new User("Civil", "civil@test.com", "pass");
        ReflectionTestUtils.setField(civil, "id", 2L);

        var token = tokenService.generateToken(civil);
        var subject = tokenService.getSubject(token);

        assertThat(subject).isEqualTo("civil@test.com");
    }
}
