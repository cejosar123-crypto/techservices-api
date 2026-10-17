package pe.techservices.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.techservices.model.UsuarioApi;

/**
 * Repositorio de usuarios autorizados.
 * Incluye el descuento atomico de tokens, seguro frente a solicitudes
 * concurrentes: la condicion tokens_disponibles > 0 forma parte del UPDATE,
 * por lo que la base de datos garantiza que nunca se descuenta un token
 * que ya no existe.
 */
@Repository
public interface UsuarioApiRepository extends JpaRepository<UsuarioApi, Long> {

    Optional<UsuarioApi> findByApiKey(String apiKey);

    /**
     * Descuenta exactamente 1 token al usuario indicado.
     *
     * @return 1 si el descuento fue exitoso; 0 si el usuario no tiene saldo
     *         suficiente (solicitud concurrente o saldo agotado).
     */
    @Modifying
    @Query("""
            UPDATE UsuarioApi u
            SET u.tokensDisponibles = u.tokensDisponibles - 1
            WHERE u.id = :id AND u.tokensDisponibles > 0
            """)
    int descontarToken(@Param("id") Long id);
}
