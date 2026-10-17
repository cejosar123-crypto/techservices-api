package pe.techservices.dto;

/**
 * Respuesta JSON estandar para los errores.
 * Ejemplo:
 * {
 *   "mensaje": "API Key no registrada",
 *   "codigo": 401
 * }
 */
public record ErrorResponse(
        String mensaje,
        int codigo) {
}
