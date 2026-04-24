package fe.banco_digital.controller;

import fe.banco_digital.dto.RegistroNuevoUsuarioRequestDTO;
import fe.banco_digital.dto.RegistroNuevoUsuarioResponseDTO;
import fe.banco_digital.dto.ValidacionIdentidadResponseDTO;
import fe.banco_digital.dto.ValidarIdentidadRequestDTO;
import fe.banco_digital.service.RegistroUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registro")
@Tag(name = "Registro de nuevos usuarios", description = "HU-01 Registro de nuevos usuarios")
public class RegistroUsuarioController {

    private final RegistroUsuarioService registroUsuarioService;

    public RegistroUsuarioController(RegistroUsuarioService registroUsuarioService) {
        this.registroUsuarioService = registroUsuarioService;
    }

    @Operation(summary = "Validar identidad", description = "Verifica si el número de documento ya existe antes de habilitar el formulario completo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Identidad disponible"),
            @ApiResponse(responseCode = "409", description = "Identificación duplicada"),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios faltantes")
    })
    @PostMapping("/validar-identidad")
    public ResponseEntity<ValidacionIdentidadResponseDTO> validarIdentidad(@Valid @RequestBody ValidarIdentidadRequestDTO dto) {
        return ResponseEntity.ok(registroUsuarioService.validarIdentidad(dto));
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Crea cliente, cuenta bancaria automática e identidad digital del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro exitoso"),
            @ApiResponse(responseCode = "409", description = "Documento, correo o username duplicados"),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios faltantes")
    })
    @PostMapping
    public ResponseEntity<RegistroNuevoUsuarioResponseDTO> registrar(@Valid @RequestBody RegistroNuevoUsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroUsuarioService.registrar(dto));
    }
}
