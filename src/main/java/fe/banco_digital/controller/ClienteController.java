package fe.banco_digital.controller;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gestión de información de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar datos del cliente",
            description = "Permite actualizar teléfono y correo electrónico. "
                    + "El documento y el número de cuenta son de solo lectura y no se pueden modificar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos actualizados correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej: formato de email incorrecto)"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Map<String, String>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarClienteDTO dto) {

        clienteService.actualizar(id, dto);
        return ResponseEntity.ok(Map.of("mensaje", "Tus datos se han actualizado correctamente"));
    }
}
