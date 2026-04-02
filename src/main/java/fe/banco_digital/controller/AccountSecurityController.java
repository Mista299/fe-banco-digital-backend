package fe.banco_digital.controller;

import fe.banco_digital.dto.BlockAccountRequest;
import fe.banco_digital.service.AccountSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/security")
public class AccountSecurityController {

    private final AccountSecurityService service;

    public AccountSecurityController(AccountSecurityService service) {
        this.service = service;
    }

    @PostMapping("/block")
    public ResponseEntity<String> blockAccount(
            @RequestBody BlockAccountRequest request) {
        
        service.blockAccount(
                request.getUserId(),
                request.getPassword()
        );

        return ResponseEntity.ok("Cuenta bloqueada exitosamente");
    }
    @PostMapping("/unlock")
public ResponseEntity<String> unlockAccount(
        @RequestBody BlockAccountRequest request) {

    service.unlockAccount(
            request.getUserId(),
            request.getPassword()
    );

    return ResponseEntity.ok("Cuenta desbloqueada exitosamente");
}
}
