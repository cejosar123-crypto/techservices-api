package pe.techservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.techservices.model.Cliente;

/**
 * Repositorio de clientes de TechServices Peru.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
