package fe.banco_digital.controller;

import fe.banco_digital.service.ExtractoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/extractos")
@Tag(name = "Extractos", description = "Generacion de extractos bancarios en PDF")
public class ExtractoController {

    private final ExtractoService extractoService;

    public ExtractoController(ExtractoService extractoService) {
        this.extractoService = extractoService;
    }

    @Operation(summary = "Descargar extracto bancario mensual en PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parametros de periodo invalidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "422", description = "El periodo aun no ha cerrado")
    })
    @GetMapping("/{idCuenta}/{anio}/{mes}")
    public ResponseEntity<byte[]> generarExtracto(
            @PathVariable Long idCuenta,
            @PathVariable int anio,
            @PathVariable int mes,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        byte[] pdf = extractoService.generarExtracto(
                idCuenta, anio, mes, usuarioAutenticado.getUsername());

        String nombreArchivo = String.format("extracto_%d_%02d.pdf", anio, mes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .body(pdf);
    }
}
