package pe.techservices.exception;

/**
 * Se lanza cuando el usuario no tiene tokens disponibles (saldo 0).
 * Corresponde a HTTP 429 Too Many Requests.
 */
public class SaldoAgotadoException extends RuntimeException {

    public SaldoAgotadoException(String mensaje) {
        super(mensaje);
    }
}
