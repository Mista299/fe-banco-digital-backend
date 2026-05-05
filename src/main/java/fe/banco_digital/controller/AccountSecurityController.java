package fe.banco_digital.controller;

import fe.banco_digital.dto.SolicitudBloqueoDTO;
import fe.banco_digital.service.AccountSecurityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas/seguridad")
public class AccountSecurityController {

    private final AccountSecurityService service;

    public AccountSecurityController(AccountSecurityService service) {
        this.service = service;
    }

    @PostMapping("/bloquear")
    public ResponseEntity<String> bloquearCuenta(
            @Valid @RequestBody SolicitudBloqueoDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        service.bloquearCuenta(usuarioAutenticado.getUsername(), solicitud.getPassword());
        return ResponseEntity.ok("Cuenta bloqueada exitosamente");
    }

    @PostMapping("/desbloquear")
    public ResponseEntity<String> desbloquearCuenta(
            @Valid @RequestBody SolicitudBloqueoDTO solicitud,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        service.desbloquearCuenta(usuarioAutenticado.getUsername(), solicitud.getPassword());
        return ResponseEntity.ok("Cuenta desbloqueada exitosamente");
    }
}
