package fe.banco_digital.service;

import fe.banco_digital.entity.*;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TokenRetiroRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TokenRetiroServiceImpl implements TokenRetiroService {

    private final CuentaRepository cuentaRepository;
    private final TokenRetiroRepository tokenRepository;

    public TokenRetiroServiceImpl(CuentaRepository cuentaRepository, TokenRetiroRepository tokenRepository) {
        this.cuentaRepository = cuentaRepository;
        this.tokenRepository = tokenRepository;
    }

    // ESCENARIO 1
    @Override
    @Transactional
    public TokenRetiro generarToken(Long idCuenta, BigDecimal monto) {

        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        // VALIDACIONES (Escenario 2)
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new RuntimeException("Cuenta no está activa");
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Monto inválido");
        }

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        // GENERAR CÓDIGO
        String codigo = generarCodigo6Digitos();

        // RESERVAR DINERO
        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuenta.setSaldoReservado(
                cuenta.getSaldoReservado().add(monto)
        );

        cuentaRepository.save(cuenta);

        //  CREAR TOKEN
        TokenRetiro token = new TokenRetiro();
        token.setCodigo(codigo);
        token.setMonto(monto);
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
        token.setCuenta(cuenta);

        return tokenRepository.save(token);
    }

    // ESCENARIO 4 (usar token)
    @Override
    @Transactional
    public void usarToken(String codigo) {

        TokenRetiro token = tokenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Token no existe"));

        if (token.getEstado() != EstadoToken.ACTIVO) {
            throw new RuntimeException("Token inválido");
        }

        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        Cuenta cuenta = token.getCuenta();

        //  EFECTIVO: descontar reservado
        cuenta.setSaldoReservado(
                cuenta.getSaldoReservado().subtract(token.getMonto())
        );

        token.setEstado(EstadoToken.USADO);

        cuentaRepository.save(cuenta);
        tokenRepository.save(token);
    }

    // ESCENARIO 3 (EXPIRACIÓN)
    @Override
    @Transactional
    public void expirarTokens() {

        tokenRepository.findAll().forEach(token -> {

            if (token.getEstado() == EstadoToken.ACTIVO &&
                token.getFechaExpiracion().isBefore(LocalDateTime.now())) {

                Cuenta cuenta = token.getCuenta();

                // DEVOLVER DINERO
                cuenta.setSaldo(
                        cuenta.getSaldo().add(token.getMonto())
                );

                cuenta.setSaldoReservado(
                        cuenta.getSaldoReservado().subtract(token.getMonto())
                );

                token.setEstado(EstadoToken.EXPIRADO);

                cuentaRepository.save(cuenta);
                tokenRepository.save(token);
            }
        });
    }

    private String generarCodigo6Digitos() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }
}
