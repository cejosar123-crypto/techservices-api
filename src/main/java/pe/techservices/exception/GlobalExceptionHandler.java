package pe.techservices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.techservices.dto.ErrorResponse;

/**
 * Convierte las excepciones en respuestas JSON con el codigo HTTP correcto.
 *
 * 401 -> Credenciales incorrectas o incompletas
 * 403 -> Usuario inactivo
 * 404 -> Cliente no existe
 * 429 -> Saldo de tokens agotado
 * 500 -> Error interno (mensaje generico, sin informacion sensible)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredenciales(CredencialesInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<ErrorResponse> manejarUsuarioInactivo(UsuarioInactivoException ex) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(SaldoAgotadoException.class)
    public ResponseEntity<ErrorResponse> manejarSaldoAgotado(SaldoAgotadoException ex) {
        return responder(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarClienteNoEncontrado(ClienteNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorInterno(Exception ex) {
        // No se expone el detalle de la excepcion: informacion sensible
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje) {
        ErrorResponse error = new ErrorResponse(mensaje, status.value());
        return ResponseEntity.status(status).body(error);
    }
}
