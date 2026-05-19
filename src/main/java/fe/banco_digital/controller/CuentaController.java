package fe.banco_digital.controller;

import fe.banco_digital.dto.AbrirCuentaSolicitudDTO;
import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas", description = "Operaciones sobre cuentas bancarias")
public class CuentaController {

    private final CuentaService cuentaService;

    // Inyección por constructor — patrón del equipo
    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Abrir nueva cuenta", description = "Abre una cuenta adicional (AHORROS o CORRIENTE) para el cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta abierta exitosamente"),
            @ApiResponse(responseCode = "400", description = "Tipo de cuenta inválido")
    })
    @PostMapping("/abrir")
    public ResponseEntity<CuentaResumenDTO> abrirCuenta(
            @Valid @RequestBody AbrirCuentaSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cuentaService.abrirCuenta(solicitud, usuarioAutenticado.getUsername()));
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
    public ResponseEntity<List<CuentaResumenDTO>> obtenerDashboard(
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        List<CuentaResumenDTO> cuentas =
                cuentaService.obtenerCuentasDelCliente(usuarioAutenticado.getUsername());
        return ResponseEntity.ok(cuentas);
    }
}