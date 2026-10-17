package pe.techservices.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pe.techservices.dto.ConsultaClienteResponse;
import pe.techservices.exception.ClienteNoEncontradoException;
import pe.techservices.model.Cliente;
import pe.techservices.model.UsuarioApi;
import pe.techservices.repository.ClienteRepository;
import pe.techservices.repository.UsuarioApiRepository;

/**
 * Pruebas unitarias del servicio de consulta de clientes (sin base de datos).
 * Verifica la regla principal del examen: el token se descuenta SOLO si
 * la consulta del cliente es exitosa.
 */
class ClienteServiceTest {

    private ClienteRepository clienteRepository;
    private UsuarioApiRepository usuarioApiRepository;
    private ClienteService servicio;

    @BeforeEach
    void preparar() {
        clienteRepository = Mockito.mock(ClienteRepository.class);
        usuarioApiRepository = Mockito.mock(UsuarioApiRepository.class);
        servicio = new ClienteService(clienteRepository, usuarioApiRepository);
    }

    private UsuarioApi crearUsuario() {
        UsuarioApi usuario = new UsuarioApi();
        usuario.setId(1L);
        usuario.setNombreUsuario("analista1");
        usuario.setApiKey("MI_API_KEY_2026");
        usuario.setTokenAutenticacion("TOKEN_SEGURO_123456");
        usuario.setTokensDisponibles(10);
        usuario.setEstado("activo");
        return usuario;
    }

    private Cliente crearCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombres("Carlos");
        cliente.setApellidos("Ramirez Soto");
        cliente.setDni("12345678");
        cliente.setCorreoElectronico("carlos.ramirez@example.com");
        cliente.setTelefono("999888777");
        return cliente;
    }

    @Test
    void consultaExitosa_descuentaUnToken_yDevuelveSaldoRestante() {
        UsuarioApi usuario = crearUsuario();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(crearCliente(1L)));
        // Simula que el UPDATE atomico desconto 1 token
        when(usuarioApiRepository.descontarToken(1L)).thenReturn(1);
        UsuarioApi usuarioActualizado = crearUsuario();
        usuarioActualizado.setTokensDisponibles(9);
        when(usuarioApiRepository.findById(1L)).thenReturn(Optional.of(usuarioActualizado));

        ConsultaClienteResponse respuesta = servicio.consultarCliente(1L, usuario);

        assertEquals("Consulta realizada correctamente", respuesta.mensaje());
        assertEquals(9, respuesta.tokensRestantes());
        assertEquals(1L, respuesta.cliente().id());
        verify(usuarioApiRepository, times(1)).descontarToken(1L);
    }

    @Test
    void clienteInexistente_noDescuentaToken() {
        UsuarioApi usuario = crearUsuario();
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class,
                () -> servicio.consultarCliente(999L, usuario));

        verify(usuarioApiRepository, never()).descontarToken(anyLong());
    }
}
