package pe.techservices.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.techservices.dto.ConsultaClienteResponse;
import pe.techservices.model.UsuarioApi;
import pe.techservices.security.AuthValidator;
import pe.techservices.service.ClienteService;

/**
 * Endpoint REST principal:
 *
 * GET /api/clientes/{id}
 * Encabezados requeridos:
 *   x-api-key: <API Key del usuario>
 *   Authorization: Bearer <token de autenticacion>
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final AuthValidator authValidator;
    private final ClienteService clienteService;

    public ClienteController(AuthValidator authValidator, ClienteService clienteService) {
        this.authValidator = authValidator;
        this.clienteService = clienteService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaClienteResponse> consultarCliente(
            @PathVariable("id") Long id,
            @RequestHeader(value = "x-api-key", required = false) String apiKey,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 1. Validar API Key + token (lanza 401 / 403 / 429 si falla)
        UsuarioApi usuario = authValidator.validar(apiKey, authorization);

        // 2. Consultar al cliente y descontar 1 token (404 si no existe)
        ConsultaClienteResponse respuesta = clienteService.consultarCliente(id, usuario);

        // 3. Respuesta exitosa HTTP 200
        return ResponseEntity.ok(respuesta);
    }
}
