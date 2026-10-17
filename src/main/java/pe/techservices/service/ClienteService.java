package pe.techservices.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.techservices.dto.ClienteDTO;
import pe.techservices.dto.ConsultaClienteResponse;
import pe.techservices.exception.ClienteNoEncontradoException;
import pe.techservices.exception.SaldoAgotadoException;
import pe.techservices.model.Cliente;
import pe.techservices.model.UsuarioApi;
import pe.techservices.repository.ClienteRepository;
import pe.techservices.repository.UsuarioApiRepository;

/**
 * Logica de negocio: consultar un cliente por ID y descontar el token.
 *
 * Orden importante de las validaciones:
 * 1. Primero se verifica que el cliente exista (si no existe, HTTP 404
 *    y NO se descuentan tokens).
 * 2. Solo si el cliente existe, se descuenta exactamente 1 token con un
 *    UPDATE atomico en la base de datos, de modo que dos solicitudes
 *    concurrentes nunca puedan descontar mas tokens de los que existen
 *    (la condicion "tokens_disponibles > 0" esta en la propia consulta SQL).
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioApiRepository usuarioApiRepository;

    public ClienteService(ClienteRepository clienteRepository, UsuarioApiRepository usuarioApiRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioApiRepository = usuarioApiRepository;
    }

    /**
     * Realiza la consulta del cliente y descuenta 1 token del usuario.
     * Todo dentro de una transaccion para que sea consistente.
     */
    @Transactional
    public ConsultaClienteResponse consultarCliente(Long clienteId, UsuarioApi usuario) {

        // Paso 1: verificar que el cliente exista (antes de descontar nada)
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No existe un cliente con el id " + clienteId));

        // Paso 2: descontar 1 token de forma ATOMICA y segura.
        // La consulta solo descuenta si tokens_disponibles > 0.
        // Devuelve el numero de filas afectadas (1 = exito, 0 = saldo agotado).
        int filasAfectadas = usuarioApiRepository.descontarToken(usuario.getId());
        if (filasAfectadas == 0) {
            throw new SaldoAgotadoException("No tiene tokens disponibles");
        }

        // Paso 3: leer el saldo real despues del descuento
        int tokensRestantes = usuarioApiRepository.findById(usuario.getId())
                .orElseThrow(() -> new SaldoAgotadoException("No tiene tokens disponibles"))
                .getTokensDisponibles();

        // Paso 4: armar la respuesta con los datos que vienen de MySQL
        return new ConsultaClienteResponse(
                "Consulta realizada correctamente",
                ClienteDTO.desde(cliente),
                tokensRestantes);
    }
}
