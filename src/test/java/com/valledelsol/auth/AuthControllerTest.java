package com.valledelsol.auth;

import com.valledelsol.auth.controller.AuthController;
import com.valledelsol.auth.domain.Role;
import com.valledelsol.auth.domain.User;
import com.valledelsol.auth.domain.UserRepository;
import com.valledelsol.auth.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — pruebas unitarias")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private User usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new User("Admin", "admin@test.com", "encodedPassword");
        usuarioPrueba.actualizarRol(Role.ADMIN);
        ReflectionTestUtils.setField(usuarioPrueba, "id", 1L);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login con credenciales válidas debe retornar 200 con token")
    void login_conCredencialesValidas_debeRetornar200ConToken() {
        var authToken = new UsernamePasswordAuthenticationToken(usuarioPrueba, null, usuarioPrueba.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(tokenService.generateToken(usuarioPrueba)).thenReturn("jwt.token.generado");

        var loginRequest = new AuthController.LoginRequest("admin@test.com", "123456");
        var response = authController.login(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("jwt.token.generado");
    }

    @Test
    @DisplayName("login con credenciales inválidas debe lanzar excepción")
    void login_conCredencialesInvalidas_debeLanzarExcepcion() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        var loginRequest = new AuthController.LoginRequest("admin@test.com", "wrongpassword");

        assertThatThrownBy(() -> authController.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ── registro ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registro con email nuevo debe retornar 201")
    void registro_conEmailNuevo_debeRetornar201() {
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(usuarioPrueba);

        var request = new AuthController.RegistroRequest("Nuevo", "nuevo@test.com", "123456");
        var response = authController.registro(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Usuario creado");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("registro con email ya existente debe retornar 400")
    void registro_conEmailExistente_debeRetornar400() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(usuarioPrueba);

        var request = new AuthController.RegistroRequest("Admin", "admin@test.com", "123456");
        var response = authController.registro(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Email ya registrado");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registro no debe guardar usuario si el email ya existe")
    void registro_conEmailExistente_noDebeGuardarUsuario() {
        when(userRepository.findByEmail(anyString())).thenReturn(usuarioPrueba);

        var request = new AuthController.RegistroRequest("Admin", "admin@test.com", "123456");
        authController.registro(request);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ── me ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("me con token válido debe retornar datos del usuario")
    void me_conTokenValido_debeRetornarDatosUsuario() {
        when(tokenService.getSubject("token.valido")).thenReturn("admin@test.com");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(usuarioPrueba);

        var response = authController.me("Bearer token.valido");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("admin@test.com");
        assertThat(response.getBody().rol()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("me debe retornar el nombre correcto del usuario")
    void me_debeRetornarNombreCorrectoDelUsuario() {
        when(tokenService.getSubject("token.valido")).thenReturn("admin@test.com");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(usuarioPrueba);

        var response = authController.me("Bearer token.valido");

        assertThat(response.getBody().nombre()).isEqualTo("Admin");
        assertThat(response.getBody().id()).isEqualTo(1L);
    }
}
