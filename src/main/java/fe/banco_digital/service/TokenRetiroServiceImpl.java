package fe.banco_digital.service;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.TokenRetiro;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TokenRetiroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TokenRetiroServiceImpl implements TokenRetiroService {

    private final TokenRetiroRepository tokenRepo;
    private final CuentaRepository cuentaRepo;

    public TokenRetiroServiceImpl(TokenRetiroRepository tokenRepo, CuentaRepository cuentaRepo) {
        this.tokenRepo = tokenRepo;
        this.cuentaRepo = cuentaRepo;
    }

    @Override
    public String generarToken(Long idCuenta) {
        Cuenta cuenta = cuentaRepo.findById(idCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        String codigo = generarCodigo6Digitos();

        TokenRetiro token = new TokenRetiro();
        token.setCodigo(codigo);
        token.setCuenta(cuenta);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(5));

        tokenRepo.save(token);

        return codigo;
    }

    @Override
    public boolean validarToken(String codigo) {
        TokenRetiro token = tokenRepo.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));

        if (token.isUsado()) {
            return false;
        }

        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }

        token.setUsado(true);
        tokenRepo.save(token);

        return true;
    }

    private String generarCodigo6Digitos() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }
}