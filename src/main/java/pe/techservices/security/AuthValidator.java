package pe.techservices.security;

import org.springframework.stereotype.Component;

import pe.techservices.exception.CredencialesInvalidasException;
import pe.techservices.exception.SaldoAgotadoException;
import pe.techservices.exception.UsuarioInactivoException;
import pe.techservices.model.UsuarioApi;
import pe.techservices.repository.UsuarioApiRepository;

/**
 * Valida la seguridad de cada solicitud:
 *
 * 1. Debe incluir el encabezado x-api-key.
 * 2. Debe incluir el encabezado Authorization con formato "Bearer <token>".
 * 3. La API Key y el token deben pertenecer al MISMO usuario autorizado.
 * 4. El usuario debe estar activo (estado = 'activo').
 * 5. El usuario debe tener al menos 1 token disponible.
 *
 * Si todo es correcto, devuelve el usuario validado para que el
 * servicio realice la consulta y descuente el token.
 */
@Component
public class AuthValidator {

    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String ESTADO_ACTIVO = "activo";

    private final UsuarioApiRepository usuarioApiRepository;

    public AuthValidator(UsuarioApiRepository usuarioApiRepository) {
        this.usuarioApiRepository = usuarioApiRepository;
    }

    /**
     * Valida los encabezados de la solicitud y devuelve el usuario autorizado.
     * Lanza excepciones con el codigo HTTP correcto si algo falla.
     */
    public UsuarioApi validar(String apiKey, String authorizationHeader) {
        // Regla 1: la solicitud debe incluir la API Key
        if (apiKey == null || apiKey.isBlank()) {
            throw new CredencialesInvalidasException("Falta el encabezado x-api-key");
        }

        // Regla 2: debe incluir Authorization con formato Bearer
        if (authorizationHeader == null || !authorizationHeader.startsWith(PREFIJO_BEARER)) {
            throw new CredencialesInvalidasException("Falta el encabezado Authorization con el formato Bearer");
        }

        String token = authorizationHeader.substring(PREFIJO_BEARER.length()).trim();

        // Regla 3 (parte A): la API Key debe pertenecer a un usuario registrado
        UsuarioApi usuario = usuarioApiRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new CredencialesInvalidasException("API Key no registrada"));

        // Regla 3 (parte B): el token debe pertenecer al mismo usuario que la API Key
        if (!usuario.getTokenAutenticacion().equals(token)) {
            throw new CredencialesInvalidasException("Token de autenticacion incorrecto");
        }

        // Regla 4: el usuario debe estar activo
        if (!ESTADO_ACTIVO.equalsIgnoreCase(usuario.getEstado())) {
            throw new UsuarioInactivoException("El usuario se encuentra inactivo");
        }

        // Regla 5: el usuario debe tener al menos un token disponible
        if (usuario.getTokensDisponibles() == null || usuario.getTokensDisponibles() < 1) {
            throw new SaldoAgotadoException("No tiene tokens disponibles");
        }

        return usuario;
    }
}
