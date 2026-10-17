package pe.techservices.dto;

/**
 * Respuesta JSON de una consulta exitosa.
 * Ejemplo:
 * {
 *   "mensaje": "Consulta realizada correctamente",
 *   "cliente": { ... },
 *   "tokensRestantes": 9
 * }
 */
public record ConsultaClienteResponse(
        String mensaje,
        ClienteDTO cliente,
        Integer tokensRestantes) {
}
