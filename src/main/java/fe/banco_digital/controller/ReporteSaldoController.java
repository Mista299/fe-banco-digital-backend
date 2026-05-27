package fe.banco_digital.controller;

import fe.banco_digital.dto.ReporteConsolidadoDTO;
import fe.banco_digital.dto.ReporteEstadoCuentaDTO;
import fe.banco_digital.dto.SaldoTiempoRealDTO;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.service.ReporteSaldoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
    public ResponseEntity<EntityModel<ReporteConsolidadoDTO>> obtenerConsolidado() {
        ReporteConsolidadoDTO dto = reporteSaldoService.obtenerConsolidadoGlobal();
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(ReporteSaldoController.class).obtenerConsolidado()).withSelfRel(),
                linkTo(methodOn(ReporteSaldoController.class).obtenerTiempoReal()).withRel("tiempo-real"),
                linkTo(methodOn(ReporteSaldoController.class).filtrarPorEstado(null)).withRel("filtrar-por-estado")
        ));
    }

    @Operation(summary = "Filtrar cuentas por estado")
    @GetMapping("/estado")
    public ResponseEntity<CollectionModel<EntityModel<ReporteEstadoCuentaDTO>>> filtrarPorEstado(
            @RequestParam EstadoCuenta estado) {
        List<EntityModel<ReporteEstadoCuentaDTO>> items = reporteSaldoService.filtrarPorEstado(estado).stream()
                .map(d -> EntityModel.of(d,
                        linkTo(methodOn(ReporteSaldoController.class).filtrarPorEstado(estado)).withRel("self")
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(items,
                linkTo(methodOn(ReporteSaldoController.class).filtrarPorEstado(estado)).withSelfRel(),
                linkTo(methodOn(ReporteSaldoController.class).obtenerConsolidado()).withRel("consolidado")
        ));
    }

    @Operation(summary = "Filtrar cuentas por rango de saldo")
    @GetMapping("/rango")
    public ResponseEntity<CollectionModel<EntityModel<SaldoTiempoRealDTO>>> filtrarPorRango(
            @RequestParam BigDecimal min,
            @RequestParam(required = false) BigDecimal max) {
        List<EntityModel<SaldoTiempoRealDTO>> items = reporteSaldoService.filtrarPorRango(min, max).stream()
                .map(d -> EntityModel.of(d,
                        linkTo(methodOn(ReporteSaldoController.class).filtrarPorRango(min, max)).withRel("self")
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(items,
                linkTo(methodOn(ReporteSaldoController.class).filtrarPorRango(min, max)).withSelfRel(),
                linkTo(methodOn(ReporteSaldoController.class).obtenerConsolidado()).withRel("consolidado")
        ));
    }

    @Operation(summary = "Visualizar saldos en tiempo real")
    @GetMapping("/tiempo-real")
    public ResponseEntity<CollectionModel<EntityModel<SaldoTiempoRealDTO>>> obtenerTiempoReal() {
        List<EntityModel<SaldoTiempoRealDTO>> items = reporteSaldoService.obtenerSaldosTiempoReal().stream()
                .map(d -> EntityModel.of(d,
                        linkTo(methodOn(ReporteSaldoController.class).obtenerTiempoReal()).withRel("self")
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(items,
                linkTo(methodOn(ReporteSaldoController.class).obtenerTiempoReal()).withSelfRel(),
                linkTo(methodOn(ReporteSaldoController.class).obtenerConsolidado()).withRel("consolidado")
        ));
    }
}