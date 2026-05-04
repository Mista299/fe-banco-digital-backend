package fe.banco_digital.controller;

import fe.banco_digital.dto.GenerarTokenRetiroSolicitudDTO;
import fe.banco_digital.dto.TokenRetiroEstadoDTO;
import fe.banco_digital.dto.TokenRetiroRespuestaDTO;
import fe.banco_digital.service.TokenRetiroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/token-retiro")
public class TokenRetiroController {

    private final TokenRetiroService service;

    public TokenRetiroController(TokenRetiroService service) {
        this.service = service;
    }

    @PostMapping("/generar")
    public ResponseEntity<TokenRetiroRespuestaDTO> generar(
            @Valid @RequestBody GenerarTokenRetiroSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        return ResponseEntity.ok(service.generarToken(solicitud, usuarioAutenticado.getUsername()));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<TokenRetiroEstadoDTO> consultarEstado(@PathVariable String codigo) {
        return ResponseEntity.ok(service.consultarEstado(codigo));
    }
}
