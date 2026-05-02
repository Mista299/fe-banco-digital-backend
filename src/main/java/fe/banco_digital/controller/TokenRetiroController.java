package fe.banco_digital.controller;

import fe.banco_digital.entity.TokenRetiro;
import fe.banco_digital.service.TokenRetiroService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/token-retiro")
public class TokenRetiroController {

    private final TokenRetiroService service;

    public TokenRetiroController(TokenRetiroService service) {
        this.service = service;
    }

    // Generar token
    @PostMapping("/generar")
    public TokenRetiro generar(
            @RequestParam Long idCuenta,
            @RequestParam BigDecimal monto) {
        return service.generarToken(idCuenta, monto);
    }

    // Usar token
    @PostMapping("/usar")
    public String usar(@RequestParam String codigo) {
        service.usarToken(codigo);
        return "Retiro exitoso";
    }
}
