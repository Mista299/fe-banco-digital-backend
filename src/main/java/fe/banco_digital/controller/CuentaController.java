package fe.banco_digital.controller;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.DashboardResponseDTO;
import fe.banco_digital.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas", description = "Operaciones sobre cuentas bancarias")
public class CuentaController {

    private final CuentaService cuentaService;

    // Inyección por constructor — patrón del equipo
    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    /**
     * PATCH /api/v1/cuentas/cerrar
     * Cubre: Escenarios 1, 2 y 4.
     */
    @Operation(
            summary = "Cerrar cuenta",
            description = "Permite al cliente cerrar su cuenta de ahorros. " +
                    "Valida identidad, saldo cero y cambia el estado a CERRADA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta cerrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Contraseña incorrecta — cierre bloqueado"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "No se puede cerrar — saldo pendiente")
    })
    @PatchMapping("/cerrar")
    public ResponseEntity<CierreCuentaRespuestaDTO> cerrarCuenta(
            @Valid @RequestBody CierreCuentaSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        CierreCuentaRespuestaDTO respuesta = cuentaService.cerrarCuenta(solicitud, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET /api/v1/cuentas/dashboard
     * Cubre: Escenario 3.
     */
    @Operation(
            summary = "Dashboard de cuentas",
            description = "Retorna todas las cuentas del cliente autenticado. " +
                    "Las cuentas cerradas incluyen etiqueta visual y tienen transacciones bloqueadas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de cuentas obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDTO> obtenerDashboard(
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        DashboardResponseDTO cuentas =
                cuentaService.obtenerCuentasDelCliente(usuarioAutenticado.getUsername());
        return ResponseEntity.ok(cuentas);
    }
}