package com.valledelsol.auth.service;

import com.valledelsol.auth.domain.User;
import com.valledelsol.auth.domain.UserRepository;
import com.valledelsol.auth.security.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService — pruebas unitarias")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new User("Admin", "admin@test.com", "password123");
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    @DisplayName("loadUserByUsername con email existente debe retornar el usuario")
    void loadUserByUsername_conEmailExistente_debeRetornarUsuario() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(usuarioPrueba);

        var result = authenticationService.loadUserByUsername("admin@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin@test.com");
        verify(userRepository, times(1)).findByEmail("admin@test.com");
    }

    @Test
    @DisplayName("loadUserByUsername con email inexistente debe lanzar UsernameNotFoundException")
    void loadUserByUsername_conEmailInexistente_debeLanzarExcepcion() {
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(null);

        assertThatThrownBy(() ->
                authenticationService.loadUserByUsername("noexiste@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(userRepository, times(1)).findByEmail("noexiste@test.com");
    }

    @Test
    @DisplayName("loadUserByUsername debe llamar al repositorio exactamente una vez")
    void loadUserByUsername_debeLlamarRepositorioUnaVez() {
        when(userRepository.findByEmail(anyString())).thenReturn(usuarioPrueba);

        authenticationService.loadUserByUsername("admin@test.com");

        verify(userRepository, times(1)).findByEmail("admin@test.com");
        verifyNoMoreInteractions(userRepository);
    }
}
