package fe.banco_digital.service;

import fe.banco_digital.repository.CuentaRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class NumeroCuentaServiceImpl implements NumeroCuentaService {

    private final CuentaRepository cuentaRepository;

    public NumeroCuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public String generarNumeroCuenta() {
        String numero;
        do {
            long valor = ThreadLocalRandom.current().nextLong(10000000L, 99999999L);
            numero = String.valueOf(valor);
        } while (cuentaRepository.existsByNumeroCuenta(numero));
        return numero;
    }
}
