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
            long sufijo = ThreadLocalRandom.current().nextLong(1000000L, 9999999L);
            numero = "500" + sufijo;
        } while (cuentaRepository.existsByNumeroCuenta(numero));
        return numero;
    }
}
