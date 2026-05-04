package fe.banco_digital.controller;

import fe.banco_digital.dto.DepositoSolicitudDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.RetiroSolicitudDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "Movimientos y consultas de transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(summary = "Depositar dinero en una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cuenta bloqueada o inactiva"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado")
    })
    @PostMapping("/depositar")
    public ResponseEntity<TransaccionRespuestaDTO> depositar(
            @Valid @RequestBody DepositoSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transaccionService.depositar(solicitud, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Retirar dinero de una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retiro realizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cuenta bloqueada o inactiva"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente")
    })
    @PostMapping("/retirar")
    public ResponseEntity<TransaccionRespuestaDTO> retirar(
            @Valid @RequestBody RetiroSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transaccionService.retirar(solicitud, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Transferir dinero entre cuentas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferencia realizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cuenta destino bloqueada o inactiva"),
            @ApiResponse(responseCode = "403", description = "La cuenta origen no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente")
    })
    @PostMapping("/transferir")
    public ResponseEntity<TransaccionRespuestaDTO> transferir(
            @Valid @RequestBody TransferenciaSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transaccionService.transferir(solicitud, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Listar movimientos de una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado")
    })
    @GetMapping("/cuenta/{idCuenta}")
    public ResponseEntity<List<MovimientoDTO>> obtenerMovimientos(
            @PathVariable Long idCuenta,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transaccionService.obtenerMovimientos(idCuenta, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Filtrar movimientos por rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimientos filtrados correctamente"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado")
    })
    @GetMapping("/cuenta/{idCuenta}/filtro")
    public ResponseEntity<List<MovimientoDTO>> obtenerMovimientosPorFecha(
            @PathVariable Long idCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(
                transaccionService.obtenerMovimientosPorFecha(
                        idCuenta, fechaInicio, fechaFin, usuarioAutenticado.getUsername()));
    }

    @Operation(summary = "Ejecutar transferencia interna",
            description = "Transfiere dinero entre dos cuentas del mismo banco de forma atómica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transferencia exitosa"),
            @ApiResponse(responseCode = "404", description = "Cuenta destino no encontrada"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente o cuenta bloqueada"),
            @ApiResponse(responseCode = "403", description = "La cuenta origen no pertenece al usuario")
    })
    @PostMapping("/transferencia")
    public ResponseEntity<TransaccionRespuestaDTO> ejecutarTransferenciaMismoBanco(
            @Valid @RequestBody TransferenciaSolicitudDTO dto,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        return ResponseEntity.ok(
                transaccionService.ejecutarTransferenciaMismoBanco(dto, usuarioAutenticado.getUsername()));
    }
}
