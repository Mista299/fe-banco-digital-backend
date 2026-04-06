package fe.banco_digital.controller;

import fe.banco_digital.dto.SolicitudBloqueoDTO;
import fe.banco_digital.service.AccountSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas/seguridad")
public class AccountSecurityController {

    private final AccountSecurityService service;

    public AccountSecurityController(AccountSecurityService service) {
        this.service = service;
    }

    @PostMapping("/bloquear")
    public ResponseEntity<String> bloquearCuenta(@RequestBody SolicitudBloqueoDTO solicitud) {
        service.bloquearCuenta(solicitud.getIdUsuario(), solicitud.getPassword());
        return ResponseEntity.ok("Cuenta bloqueada exitosamente");
    }

    @PostMapping("/desbloquear")
    public ResponseEntity<String> desbloquearCuenta(@RequestBody SolicitudBloqueoDTO solicitud) {
        service.desbloquearCuenta(solicitud.getIdUsuario(), solicitud.getPassword());
        return ResponseEntity.ok("Cuenta desbloqueada exitosamente");
    }
}
