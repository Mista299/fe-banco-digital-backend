package fe.banco_digital.controller;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.dto.DashboardClienteDTO;
import fe.banco_digital.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gestión de información de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard del cliente", description = "Retorna nombre, email y cuentas activas del cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<EntityModel<DashboardClienteDTO>> dashboard(
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        DashboardClienteDTO dto = clienteService.obtenerDashboard(usuarioAutenticado.getUsername());
        EntityModel<DashboardClienteDTO> model = EntityModel.of(dto,
                linkTo(methodOn(ClienteController.class).dashboard(null)).withSelfRel(),
                linkTo(methodOn(CuentaController.class).obtenerDashboard(null)).withRel("mis-cuentas")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping("/me/rol")
    @Operation(summary = "Rol del usuario autenticado", description = "Retorna el rol principal del usuario en sesión")
    public ResponseEntity<EntityModel<Map<String, String>>> getMiRol(
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        String rol = usuarioAutenticado.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .filter(a -> !a.equals("CLIENTE"))
                .findFirst()
                .orElse("CLIENTE");
        return ResponseEntity.ok(EntityModel.of(
                Map.of("rol", rol),
                linkTo(methodOn(ClienteController.class).getMiRol(null)).withSelfRel(),
                linkTo(methodOn(ClienteController.class).dashboard(null)).withRel("dashboard")
        ));
    }

    @PutMapping("/me")
    @Operation(
            summary = "Actualizar datos del cliente autenticado",
            description = "Permite actualizar teléfono y correo electrónico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos actualizados correctamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> actualizar(
            @Valid @RequestBody ActualizarClienteDTO dto,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        clienteService.actualizar(dto, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(EntityModel.of(
                Map.of("mensaje", "Tus datos se han actualizado correctamente"),
                linkTo(methodOn(ClienteController.class).actualizar(null, null)).withSelfRel(),
                linkTo(methodOn(ClienteController.class).dashboard(null)).withRel("dashboard")
        ));
    }
}
