package fe.banco_digital.controller;

import fe.banco_digital.dto.DecisionAperturaRespuestaDTO;
import fe.banco_digital.dto.SolicitudPendienteDTO;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.service.AdminCuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/admin/cuentas")
@Tag(name = "Admin - Cuentas", description = "Gestión de solicitudes de apertura de cuentas por parte del administrador")
public class AdminCuentaController {

    private static final String REL_PENDIENTES = "pendientes";

    private final AdminCuentaService adminCuentaService;

    public AdminCuentaController(AdminCuentaService adminCuentaService) {
        this.adminCuentaService = adminCuentaService;
    }

    @Operation(summary = "Listar solicitudes pendientes",
            description = "Retorna todas las cuentas en estado PENDIENTE_APROBACION.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @GetMapping("/pendientes")
    public ResponseEntity<CollectionModel<EntityModel<SolicitudPendienteDTO>>> listarPendientes() {
        List<EntityModel<SolicitudPendienteDTO>> items = adminCuentaService.listarPendientes().stream()
                .map(s -> EntityModel.of(s,
                        linkTo(methodOn(AdminCuentaController.class).aprobarApertura(s.getIdCuenta(), null)).withRel("aprobar"),
                        linkTo(methodOn(AdminCuentaController.class).rechazarApertura(s.getIdCuenta(), null)).withRel("rechazar")
                ))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(items,
                linkTo(methodOn(AdminCuentaController.class).listarPendientes()).withSelfRel()
        ));
    }

    @Operation(summary = "Aprobar apertura de cuenta",
            description = "Activa la cuenta que estaba en PENDIENTE_APROBACION.")
    @ApiResponse(responseCode = "200", description = "Cuenta aprobada exitosamente")
    @ApiResponse(responseCode = "400", description = "La cuenta no está en estado pendiente")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<EntityModel<DecisionAperturaRespuestaDTO>> aprobarApertura(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        DecisionAperturaRespuestaDTO dto = adminCuentaService.aprobarApertura(id, admin.getUsername());
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(AdminCuentaController.class).aprobarApertura(id, null)).withSelfRel(),
                linkTo(methodOn(AdminCuentaController.class).listarPendientes()).withRel(REL_PENDIENTES)
        ));
    }

    @Operation(summary = "Rechazar apertura de cuenta",
            description = "Marca como INACTIVA la cuenta que estaba en PENDIENTE_APROBACION.")
    @ApiResponse(responseCode = "200", description = "Solicitud rechazada exitosamente")
    @ApiResponse(responseCode = "400", description = "La cuenta no está en estado pendiente")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<EntityModel<DecisionAperturaRespuestaDTO>> rechazarApertura(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        DecisionAperturaRespuestaDTO dto = adminCuentaService.rechazarApertura(id, admin.getUsername());
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(AdminCuentaController.class).rechazarApertura(id, null)).withSelfRel(),
                linkTo(methodOn(AdminCuentaController.class).listarPendientes()).withRel(REL_PENDIENTES)
        ));
    }

    @Operation(summary = "Procesar decisión sobre solicitud de apertura",
            description = "Acepta {\"decision\":\"APROBAR\"} o {\"decision\":\"RECHAZAR\"}")
    @ApiResponse(responseCode = "200", description = "Decisión aplicada exitosamente")
    @ApiResponse(responseCode = "400", description = "Decisión inválida o cuenta no pendiente")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    @PostMapping("/{id}/decision")
    public ResponseEntity<EntityModel<DecisionAperturaRespuestaDTO>> procesarDecision(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails admin) {
        String decision = body.getOrDefault("decision", "");
        DecisionAperturaRespuestaDTO dto = switch (decision) {
            case "APROBAR" -> adminCuentaService.aprobarApertura(id, admin.getUsername());
            case "RECHAZAR" -> adminCuentaService.rechazarApertura(id, admin.getUsername());
            default -> throw new OperacionNoPermitidaException("Decisión inválida. Use APROBAR o RECHAZAR.");
        };
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(AdminCuentaController.class).procesarDecision(id, null, null)).withSelfRel(),
                linkTo(methodOn(AdminCuentaController.class).listarPendientes()).withRel(REL_PENDIENTES)
        ));
    }
}
