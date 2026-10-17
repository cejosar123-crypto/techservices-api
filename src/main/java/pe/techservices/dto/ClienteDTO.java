package pe.techservices.dto;

import pe.techservices.model.Cliente;

/**
 * Datos del cliente que se devuelven en el JSON de respuesta.
 * Usa los nombres camelCase que se muestran en la respuesta (correoElectronico).
 */
public record ClienteDTO(
        Long id,
        String nombres,
        String apellidos,
        String dni,
        String correoElectronico,
        String telefono) {

    public static ClienteDTO desde(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getDni(),
                cliente.getCorreoElectronico(),
                cliente.getTelefono());
    }
}
