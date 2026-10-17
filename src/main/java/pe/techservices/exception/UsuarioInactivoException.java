package pe.techservices.exception;

/**
 * Se lanza cuando el usuario existe pero su estado es 'inactivo'.
 * Corresponde a HTTP 403 Forbidden.
 */
public class UsuarioInactivoException extends RuntimeException {

    public UsuarioInactivoException(String mensaje) {
        super(mensaje);
    }
}
