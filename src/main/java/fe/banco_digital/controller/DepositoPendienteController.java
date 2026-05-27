package fe.banco_digital.controller;

import fe.banco_digital.dto.DepositoPendienteRespuestaDTO;
import fe.banco_digital.dto.RegistrarDepositoPendienteDTO;
import fe.banco_digital.service.DepositoPendienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transacciones/deposito-pendiente")
@Tag(name = "Depósito Pendiente", description = "Registro y consulta de códigos de pago para punto físico")
public class DepositoPendienteController {

    private final DepositoPendienteService depositoPendienteService;

    public DepositoPendienteController(DepositoPendienteService depositoPendienteService) {
        this.depositoPendienteService = depositoPendienteService;
    }

    @Operation(summary = "Registrar código de pago",
               description = "Genera un depósito pendiente vinculando referencia, cuenta y monto. Expira en 15 minutos.")
    @PostMapping
    public ResponseEntity<DepositoPendienteRespuestaDTO> registrar(
            @Valid @RequestBody RegistrarDepositoPendienteDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                depositoPendienteService.registrar(solicitud, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Consultar estado del código de pago")
    @GetMapping("/{referencia}")
    public ResponseEntity<DepositoPendienteRespuestaDTO> consultar(
            @PathVariable String referencia,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                depositoPendienteService.consultar(referencia, usuarioAutenticado.getUsername()));
    }
}
