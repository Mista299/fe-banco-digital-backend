package fe.banco_digital.controller;

import fe.banco_digital.dto.DecisionAperturaRespuestaDTO;
import fe.banco_digital.dto.SolicitudPendienteDTO;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.service.AdminCuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/cuentas")
@Tag(name = "Admin - Cuentas", description = "Gestión de solicitudes de apertura de cuentas por parte del administrador")
public class AdminCuentaController {

    private final AdminCuentaService adminCuentaService;

    public AdminCuentaController(AdminCuentaService adminCuentaService) {
        this.adminCuentaService = adminCuentaService;
    }

    @Operation(summary = "Listar solicitudes pendientes",
            description = "Retorna todas las cuentas en estado PENDIENTE_APROBACION.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudPendienteDTO>> listarPendientes() {
        return ResponseEntity.ok(adminCuentaService.listarPendientes());
    }

    @Operation(summary = "Aprobar apertura de cuenta",
            description = "Activa la cuenta que estaba en PENDIENTE_APROBACION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta aprobada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La cuenta no está en estado pendiente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<DecisionAperturaRespuestaDTO> aprobarApertura(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(adminCuentaService.aprobarApertura(id, admin.getUsername()));
    }

    @Operation(summary = "Rechazar apertura de cuenta",
            description = "Marca como INACTIVA la cuenta que estaba en PENDIENTE_APROBACION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud rechazada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La cuenta no está en estado pendiente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<DecisionAperturaRespuestaDTO> rechazarApertura(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(adminCuentaService.rechazarApertura(id, admin.getUsername()));
    }

    @Operation(summary = "Procesar decisión sobre solicitud de apertura",
            description = "Acepta {\"decision\":\"APROBAR\"} o {\"decision\":\"RECHAZAR\"}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decisión aplicada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Decisión inválida o cuenta no pendiente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PostMapping("/{id}/decision")
    public ResponseEntity<DecisionAperturaRespuestaDTO> procesarDecision(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails admin) {
        String decision = body.getOrDefault("decision", "");
        return switch (decision) {
            case "APROBAR" -> ResponseEntity.ok(adminCuentaService.aprobarApertura(id, admin.getUsername()));
            case "RECHAZAR" -> ResponseEntity.ok(adminCuentaService.rechazarApertura(id, admin.getUsername()));
            default -> throw new OperacionNoPermitidaException("Decisión inválida. Use APROBAR o RECHAZAR.");
        };
    }
}
