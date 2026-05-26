package fe.banco_digital.controller;

import fe.banco_digital.dto.ReporteResumenDTO;
import fe.banco_digital.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ReporteResumenDTO generarReporte(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin
    ) {
        return reporteService.generarReporte(inicio, fin);
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