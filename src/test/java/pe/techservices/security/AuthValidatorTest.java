package pe.techservices.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pe.techservices.exception.CredencialesInvalidasException;
import pe.techservices.exception.SaldoAgotadoException;
import pe.techservices.exception.UsuarioInactivoException;
import pe.techservices.model.UsuarioApi;
import pe.techservices.repository.UsuarioApiRepository;

/**
 * Pruebas unitarias del validador de seguridad (sin base de datos).
 * Verifica los codigos HTTP logicos: 401, 403 y 429.
 */
class AuthValidatorTest {

    private UsuarioApiRepository repositorio;
    private AuthValidator validador;

    @BeforeEach
    void preparar() {
        repositorio = Mockito.mock(UsuarioApiRepository.class);
        validador = new AuthValidator(repositorio);
    }

    private UsuarioApi crearUsuarioActivo(int tokens) {
        UsuarioApi usuario = new UsuarioApi();
        usuario.setId(1L);
        usuario.setNombreUsuario("analista1");
        usuario.setApiKey("MI_API_KEY_2026");
        usuario.setTokenAutenticacion("TOKEN_SEGURO_123456");
        usuario.setTokensDisponibles(tokens);
        usuario.setEstado("activo");
        return usuario;
    }

    @Test
    void faltaApiKey_lanzaExcepcion401() {
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> validador.validar(null, "Bearer TOKEN_SEGURO_123456"));
        assertEquals("Falta el encabezado x-api-key", ex.getMessage());
    }

    @Test
    void faltaAuthorization_lanzaExcepcion401() {
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> validador.validar("MI_API_KEY_2026", null));
        assertEquals("Falta el encabezado Authorization con el formato Bearer", ex.getMessage());
    }

    @Test
    void apiKeyNoRegistrada_lanzaExcepcion401() {
        when(repositorio.findByApiKey("clave_falsa")).thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> validador.validar("clave_falsa", "Bearer TOKEN_SEGURO_123456"));
        assertEquals("API Key no registrada", ex.getMessage());
    }

    @Test
    void tokenIncorrecto_lanzaExcepcion401() {
        when(repositorio.findByApiKey("MI_API_KEY_2026")).thenReturn(Optional.of(crearUsuarioActivo(10)));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> validador.validar("MI_API_KEY_2026", "Bearer TOKEN_INCORRECTO"));
        assertEquals("Token de autenticacion incorrecto", ex.getMessage());
    }

    @Test
    void usuarioInactivo_lanzaExcepcion403() {
        UsuarioApi usuario = crearUsuarioActivo(5);
        usuario.setEstado("inactivo");
        when(repositorio.findByApiKey("MI_API_KEY_2026")).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioInactivoException.class,
                () -> validador.validar("MI_API_KEY_2026", "Bearer TOKEN_SEGURO_123456"));
    }

    @Test
    void saldoAgotado_lanzaExcepcion429() {
        when(repositorio.findByApiKey("MI_API_KEY_2026")).thenReturn(Optional.of(crearUsuarioActivo(0)));

        assertThrows(SaldoAgotadoException.class,
                () -> validador.validar("MI_API_KEY_2026", "Bearer TOKEN_SEGURO_123456"));
    }

    @Test
    void credencialesCorrectas_devuelveUsuario() {
        when(repositorio.findByApiKey("MI_API_KEY_2026")).thenReturn(Optional.of(crearUsuarioActivo(10)));

        UsuarioApi usuario = validador.validar("MI_API_KEY_2026", "Bearer TOKEN_SEGURO_123456");
        assertNotNull(usuario);
        assertEquals("analista1", usuario.getNombreUsuario());
    }
}
