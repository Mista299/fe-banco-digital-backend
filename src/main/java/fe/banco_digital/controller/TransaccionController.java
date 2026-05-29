package fe.banco_digital.controller;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "Movimientos y consultas de transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(summary = "Transferir dinero entre cuentas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferencia realizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cuenta destino bloqueada o inactiva"),
            @ApiResponse(responseCode = "403", description = "La cuenta origen no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente")
    })
    @PostMapping("/transferir")
    public ResponseEntity<EntityModel<TransaccionRespuestaDTO>> transferir(
            @Valid @RequestBody TransferenciaSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        TransaccionRespuestaDTO dto = transaccionService.transferir(solicitud, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(TransaccionController.class).transferir(null, null)).withSelfRel(),
                linkTo(methodOn(CuentaController.class).obtenerDashboard(null)).withRel("mis-cuentas")
        ));
    }

    @Operation(summary = "Listar movimientos de una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado")
    })
    @GetMapping("/cuenta/{idCuenta}")
    public ResponseEntity<List<EntityModel<MovimientoDTO>>> obtenerMovimientos(
            @PathVariable Long idCuenta,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        List<MovimientoDTO> movimientos = transaccionService.obtenerMovimientos(idCuenta, usuarioAutenticado.getUsername());
        List<EntityModel<MovimientoDTO>> modelos = movimientos.stream()
                .map(m -> EntityModel.of(m,
                        linkTo(methodOn(TransaccionController.class).obtenerMovimientos(idCuenta, null)).withRel("historial-cuenta")
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(modelos);
    }

    @Operation(summary = "Filtrar movimientos por rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimientos filtrados correctamente"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado")
    })
    @GetMapping("/cuenta/{idCuenta}/filtro")
    public ResponseEntity<List<EntityModel<MovimientoDTO>>> obtenerMovimientosPorFecha(
            @PathVariable Long idCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        List<MovimientoDTO> movimientos = transaccionService.obtenerMovimientosPorFecha(
                idCuenta, fechaInicio, fechaFin, usuarioAutenticado.getUsername());
        List<EntityModel<MovimientoDTO>> modelos = movimientos.stream()
                .map(m -> EntityModel.of(m,
                        linkTo(methodOn(TransaccionController.class).obtenerMovimientos(idCuenta, null)).withRel("historial-cuenta")
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(modelos);
    }
}
