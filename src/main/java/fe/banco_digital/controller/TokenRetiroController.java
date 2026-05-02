package fe.banco_digital.controller;

import fe.banco_digital.service.TokenRetiroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/retiro")
public class TokenRetiroController {

    private final TokenRetiroService service;

    public TokenRetiroController(TokenRetiroService service) {
        this.service = service;
    }

    @PostMapping("/generar/{idCuenta}")
    public ResponseEntity<String> generar(@PathVariable Long idCuenta) {
        String codigo = service.generarToken(idCuenta);
        return ResponseEntity.ok(codigo);
    }

    @PostMapping("/validar/{codigo}")
    public ResponseEntity<String> validar(@PathVariable String codigo) {
        boolean valido = service.validarToken(codigo);

        if (valido) {
            return ResponseEntity.ok("Token válido. Puede retirar dinero.");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado.");
        }
    }
}