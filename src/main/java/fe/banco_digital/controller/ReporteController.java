package fe.banco_digital.controller;

import fe.banco_digital.dto.ReporteResumenDTO;
import fe.banco_digital.service.ReporteService;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ResponseEntity<EntityModel<ReporteResumenDTO>> generarReporte(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin
    ) {
        ReporteResumenDTO dto = reporteService.generarReporte(inicio, fin);
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(ReporteController.class).generarReporte(inicio, fin)).withSelfRel(),
                linkTo(methodOn(ReporteSaldoController.class).obtenerConsolidado()).withRel("consolidado-saldos")
        ));
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportarCSV(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin
    ) {

        byte[] archivo =
                reporteService.exportarCSV(inicio, fin);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte.csv"
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo);
    }
}