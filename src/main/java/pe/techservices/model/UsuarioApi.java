package pe.techservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuario autorizado para consumir la API.
 * Cada usuario tiene su API Key, su token de autenticacion y un saldo de tokens.
 */
@Entity
@Table(name = "usuarios_api")
public class UsuarioApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 60)
    private String nombreUsuario;

    @Column(name = "api_key", nullable = false, unique = true, length = 100)
    private String apiKey;

    @Column(name = "token_autenticacion", nullable = false, length = 100)
    private String tokenAutenticacion;

    @Column(name = "tokens_disponibles", nullable = false)
    private Integer tokensDisponibles;

    /** Valores permitidos: activo | inactivo */
    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    public UsuarioApi() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTokenAutenticacion() {
        return tokenAutenticacion;
    }

    public void setTokenAutenticacion(String tokenAutenticacion) {
        this.tokenAutenticacion = tokenAutenticacion;
    }

    public Integer getTokensDisponibles() {
        return tokensDisponibles;
    }

    public void setTokensDisponibles(Integer tokensDisponibles) {
        this.tokensDisponibles = tokensDisponibles;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
