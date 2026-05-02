package fe.banco_digital.service;

import fe.banco_digital.entity.TokenRetiro;

import java.math.BigDecimal;

public interface TokenRetiroService {

    TokenRetiro generarToken(Long idCuenta, BigDecimal monto);

    void usarToken(String codigo);

    void expirarTokens();
}
