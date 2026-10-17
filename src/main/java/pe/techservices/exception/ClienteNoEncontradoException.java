package pe.techservices.exception;

/**
 * Se lanza cuando el cliente solicitado no existe en la base de datos.
 * Corresponde a HTTP 404 Not Found. En este caso NO se descuentan tokens.
 */
public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
