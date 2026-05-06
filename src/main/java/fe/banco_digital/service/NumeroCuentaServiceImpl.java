package fe.banco_digital.service;

import fe.banco_digital.repository.CuentaRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class NumeroCuentaServiceImpl implements NumeroCuentaService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long RANGO = 90_000_000L;
    private static final long MINIMO = 10_000_000L;

    private final CuentaRepository cuentaRepository;

    public NumeroCuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public String generarNumeroCuenta() {
        String numero;
        do {
            long valor = MINIMO + secureRandom.nextLong(RANGO);
            numero = String.valueOf(valor);
        } while (cuentaRepository.existsByNumeroCuenta(numero));
        return numero;
    }
}