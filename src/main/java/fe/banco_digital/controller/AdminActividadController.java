package fe.banco_digital.controller;

import fe.banco_digital.dto.ActividadClienteResponseDTO;
import fe.banco_digital.service.AdminActividadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/clientes")
@Tag(name = "Admin - Actividad", description = "Consulta de actividad financiera de clientes")
public class AdminActividadController {

    private final AdminActividadService adminActividadService;

    public AdminActividadController(AdminActividadService adminActividadService) {
        this.adminActividadService = adminActividadService;
    }

    @GetMapping("/buscar/documento")
    public ResponseEntity<ActividadClienteResponseDTO> porDocumento(
            @RequestParam String documento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String tipo,      // DEPOSITO, RETIRO, TRANSFERENCIA, etc.
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(adminActividadService.consultarActividadPorDocumento(
                documento, fechaInicio, fechaFin, tipo, admin.getUsername()));
    }

    @GetMapping("/buscar/cuenta")
    public ResponseEntity<ActividadClienteResponseDTO> porCuenta(
            @RequestParam String numeroCuenta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String tipo,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(adminActividadService.consultarActividadPorNumeroCuenta(
                numeroCuenta, fechaInicio, fechaFin, tipo, admin.getUsername()));
    }
}