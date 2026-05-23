package fe.banco_digital.controller;

import fe.banco_digital.dto.ConfirmacionSwiftSolicitudDTO;
import fe.banco_digital.dto.RechazoSwiftSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInternacionalResponseDTO;
import fe.banco_digital.dto.TransferenciaInternacionalSolicitudDTO;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.service.TransferenciaInternacionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transferencias/internacionales")
@Tag(name = "Transferencias Internacionales", description = "Transferencias internacionales mediante red SWIFT")
public class TransferenciaInternacionalController {

    private final TransferenciaInternacionalService transferenciaInternacionalService;

    @Value("${GATEWAY_SECRET}")
    private String gatewaySecret;

    public TransferenciaInternacionalController(TransferenciaInternacionalService transferenciaInternacionalService) {
        this.transferenciaInternacionalService = transferenciaInternacionalService;
    }

    @Operation(summary = "Crear transferencia internacional SWIFT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden SWIFT creada y pendiente de procesamiento"),
            @ApiResponse(responseCode = "400", description = "Cuenta bloqueada, inactiva o datos inválidos"),
            @ApiResponse(responseCode = "403", description = "La cuenta origen no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente")
    })
    @PostMapping
    public ResponseEntity<TransferenciaInternacionalResponseDTO> crear(
            @Valid @RequestBody TransferenciaInternacionalSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transferenciaInternacionalService.iniciarTransferencia(
                        solicitud, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Registrar confirmación SWIFT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferencia confirmada como exitosa"),
            @ApiResponse(responseCode = "404", description = "Transferencia no encontrada"),
            @ApiResponse(responseCode = "400", description = "La transferencia no está pendiente")
    })
    @PostMapping("/{idTransfInt}/confirmacion-swift")
    public ResponseEntity<TransferenciaInternacionalResponseDTO> registrarConfirmacionSwift(
            @PathVariable Long idTransfInt,
            @RequestHeader("X-Gateway-Secret") String secret,
            @RequestBody ConfirmacionSwiftSolicitudDTO solicitud) {
        if (!gatewaySecret.equals(secret)) throw new AccesoNoAutorizadoException();
        return ResponseEntity.ok(
                transferenciaInternacionalService.registrarConfirmacionSwift(idTransfInt, solicitud));
    }

    @Operation(summary = "Registrar rechazo SWIFT y reversar fondos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reversión SWIFT ejecutada"),
            @ApiResponse(responseCode = "404", description = "Transferencia no encontrada"),
            @ApiResponse(responseCode = "400", description = "La transferencia no está pendiente")
    })
    @PostMapping("/{idTransfInt}/rechazo-swift")
    public ResponseEntity<TransferenciaInternacionalResponseDTO> registrarRechazoSwift(
            @PathVariable Long idTransfInt,
            @RequestHeader("X-Gateway-Secret") String secret,
            @Valid @RequestBody RechazoSwiftSolicitudDTO solicitud) {
        if (!gatewaySecret.equals(secret)) throw new AccesoNoAutorizadoException();
        return ResponseEntity.ok(
                transferenciaInternacionalService.registrarRechazoSwift(idTransfInt, solicitud));
    }

    @Operation(summary = "Consultar estado de una transferencia internacional")
    @GetMapping("/{idTransfInt}")
    public ResponseEntity<TransferenciaInternacionalResponseDTO> consultar(
            @PathVariable Long idTransfInt,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transferenciaInternacionalService.consultarTransferencia(
                        idTransfInt, usuarioAutenticado.getUsername()));
    }
}
