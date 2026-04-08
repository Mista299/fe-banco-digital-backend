package fe.banco_digital.controller;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Listar movimientos de una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida")
    })
    @GetMapping("/cuenta/{idCuenta}")
    public ResponseEntity<List<MovimientoDTO>> obtenerMovimientos(
            @PathVariable Long idCuenta) {

        return ResponseEntity.ok(
                transaccionService.obtenerMovimientos(idCuenta));
    }

    @Operation(summary = "Filtrar movimientos por rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimientos filtrados correctamente")
    })
    @GetMapping("/cuenta/{idCuenta}/filtro")
    public ResponseEntity<List<MovimientoDTO>> obtenerMovimientosPorFecha(
            @PathVariable Long idCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        return ResponseEntity.ok(
                transaccionService.obtenerMovimientosPorFecha(
                        idCuenta, fechaInicio, fechaFin));
    }
}
