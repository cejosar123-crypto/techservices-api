package pe.techservices.exception;

/**
 * Se lanza cuando las credenciales (API Key o token) son incorrectas
 * o estan incompletas. Corresponde a HTTP 401 Unauthorized.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
