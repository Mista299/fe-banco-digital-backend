package fe.banco_digital.controller;

import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.dto.ValidacionTransaccionSolicitudDTO;
import fe.banco_digital.service.MotorValidacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/validaciones")
@Tag(name = "Motor de Validación", description = "Validación automática de saldo y estado de cuenta antes de operar")
public class MotorValidacionController {

    private final MotorValidacionService motorValidacionService;

    public MotorValidacionController(MotorValidacionService motorValidacionService) {
        this.motorValidacionService = motorValidacionService;
    }

    @Operation(summary = "Validar saldo disponible y estado de cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado de validación generado"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado")
    })
    @PostMapping("/transaccion")
    public ResponseEntity<EntityModel<ValidacionTransaccionResponseDTO>> validarTransaccion(
            @Valid @RequestBody ValidacionTransaccionSolicitudDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        ValidacionTransaccionResponseDTO dto = motorValidacionService.validar(solicitud, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(MotorValidacionController.class).validarTransaccion(null, null)).withSelfRel(),
                linkTo(methodOn(CuentaController.class).obtenerDashboard(null)).withRel("mis-cuentas")
        ));
    }
}
