package fe.banco_digital.controller;

import fe.banco_digital.dto.ConfirmacionAchSolicitudDTO;
import fe.banco_digital.dto.RechazoAchSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaResponseDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaSolicitudDTO;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.service.TransferenciaInterbancariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transferencias/interbancarias")
@Tag(name = "Transferencias Interbancarias", description = "Transferencias a otros bancos nacionales mediante proceso ACH")
public class TransferenciaInterbancariaController {

    private final TransferenciaInterbancariaService transferenciaInterbancariaService;

    @Value("${GATEWAY_SECRET}")
    private String gatewaySecret;

    public TransferenciaInterbancariaController(TransferenciaInterbancariaService transferenciaInterbancariaService) {
        this.transferenciaInterbancariaService = transferenciaInterbancariaService;
    }

    @Operation(summary = "Crear transferencia interbancaria hacia otro banco nacional")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden ACH creada y pendiente de procesamiento"),
            @ApiResponse(responseCode = "400", description = "Cuenta bloqueada, inactiva o datos inválidos"),
            @ApiResponse(responseCode = "403", description = "La cuenta origen no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente")
    })
    @PostMapping
    public ResponseEntity<TransferenciaInterbancariaResponseDTO> crear(
            @Valid @RequestBody TransferenciaInterbancariaSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transferenciaInterbancariaService.iniciarTransferencia(solicitud, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Registrar rechazo de la red ACH y reversar fondos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reversión ACH ejecutada"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
            @ApiResponse(responseCode = "400", description = "La transacción no está pendiente o no es interbancaria")
    })
    @PostMapping("/{idTransaccion}/rechazo-ach")
    public ResponseEntity<TransferenciaInterbancariaResponseDTO> registrarRechazoAch(
            @PathVariable Long idTransaccion,
            @RequestHeader("X-Gateway-Secret") String secret,
            @Valid @RequestBody RechazoAchSolicitudDTO solicitud) {
        if (!gatewaySecret.equals(secret)) throw new AccesoNoAutorizadoException();
        return ResponseEntity.ok(
                transferenciaInterbancariaService.registrarRechazoAch(idTransaccion, solicitud));
    }

    @Operation(summary = "Registrar confirmación de la red ACH como exitosa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferencia confirmada como exitosa"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
            @ApiResponse(responseCode = "400", description = "La transacción no está pendiente o no es interbancaria")
    })
    @PostMapping("/{idTransaccion}/confirmacion-ach")
    public ResponseEntity<TransferenciaInterbancariaResponseDTO> registrarConfirmacionAch(
            @PathVariable Long idTransaccion,
            @RequestHeader("X-Gateway-Secret") String secret,
            @RequestBody ConfirmacionAchSolicitudDTO solicitud) {
        if (!gatewaySecret.equals(secret)) throw new AccesoNoAutorizadoException();
        return ResponseEntity.ok(
                transferenciaInterbancariaService.registrarConfirmacionAch(idTransaccion, solicitud));
    }
}
