package fe.banco_digital.controller;

import fe.banco_digital.dto.NotificacionRetiroDTO;
import fe.banco_digital.service.TokenRetiroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/retiros")
public class RetiroGatewayController {

    private final TokenRetiroService service;

    public RetiroGatewayController(TokenRetiroService service) {
        this.service = service;
    }

    @PostMapping("/notificacion")
    public ResponseEntity<Map<String, Object>> procesarRetiro(
            @Valid @RequestBody NotificacionRetiroDTO notificacion) {
        service.usarToken(notificacion.getCodigoToken(), notificacion.getMonto());
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("estado", 200);
        respuesta.put("mensaje", "Retiro procesado exitosamente");
        return ResponseEntity.ok(respuesta);
    }
}
