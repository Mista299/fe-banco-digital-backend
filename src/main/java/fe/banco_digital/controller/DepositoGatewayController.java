package fe.banco_digital.controller;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.service.DepositoGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/depositos")
@Tag(name = "Depósitos", description = "Notificaciones de abono desde pasarela de pagos")
public class DepositoGatewayController {

    private final DepositoGatewayService depositoGatewayService;

    public DepositoGatewayController(DepositoGatewayService depositoGatewayService) {
        this.depositoGatewayService = depositoGatewayService;
    }

    @Operation(summary = "Notificación de depósito desde pasarela",
               description = "Recibe la confirmación de un abono externo y lo acredita en la cuenta destino.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito acreditado — comprobante generado"),
            @ApiResponse(responseCode = "400", description = "Datos de la notificación inválidos"),
            @ApiResponse(responseCode = "422", description = "Depósito rechazado — cuenta bloqueada, cerrada o no encontrada")
    })
    @PostMapping("/notificacion")
    public ResponseEntity<ComprobanteDepositoDTO> recibirNotificacion(
            @Valid @RequestBody NotificacionDepositoDTO notificacion) {
        return ResponseEntity.ok(depositoGatewayService.procesarNotificacion(notificacion));
    }
}
