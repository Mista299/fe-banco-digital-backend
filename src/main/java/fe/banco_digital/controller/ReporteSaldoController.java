package fe.banco_digital.controller;

import fe.banco_digital.dto.ReporteConsolidadoDTO;
import fe.banco_digital.dto.ReporteEstadoCuentaDTO;
import fe.banco_digital.dto.SaldoTiempoRealDTO;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.service.ReporteSaldoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/saldos")
@Tag(name = "Reportes de saldos", description = "Reportes administrativos de cuentas")
public class ReporteSaldoController {

    private final ReporteSaldoService reporteSaldoService;

    public ReporteSaldoController(ReporteSaldoService reporteSaldoService) {
        this.reporteSaldoService = reporteSaldoService;
    }

    @Operation(summary = "Consolidado global de saldos")
    @GetMapping("/consolidado")
    public ResponseEntity<ReporteConsolidadoDTO> obtenerConsolidado() {

        return ResponseEntity.ok(
                reporteSaldoService.obtenerConsolidadoGlobal()
        );
    }

    @Operation(summary = "Filtrar cuentas por estado")
    @GetMapping("/estado")
    public ResponseEntity<List<ReporteEstadoCuentaDTO>> filtrarPorEstado(
            @RequestParam EstadoCuenta estado) {

        return ResponseEntity.ok(
                reporteSaldoService.filtrarPorEstado(estado)
        );
    }

    @Operation(summary = "Filtrar cuentas por rango de saldo")
    @GetMapping("/rango")
    public ResponseEntity<List<SaldoTiempoRealDTO>> filtrarPorRango(
            @RequestParam BigDecimal min,
            @RequestParam(required = false) BigDecimal max) {

        return ResponseEntity.ok(
                reporteSaldoService.filtrarPorRango(min, max)
        );
    }

    @Operation(summary = "Visualizar saldos en tiempo real")
    @GetMapping("/tiempo-real")
    public ResponseEntity<List<SaldoTiempoRealDTO>> obtenerTiempoReal() {

        return ResponseEntity.ok(
                reporteSaldoService.obtenerSaldosTiempoReal()
        );
    }
}