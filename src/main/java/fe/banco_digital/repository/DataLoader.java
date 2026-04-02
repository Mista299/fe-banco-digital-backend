package fe.banco_digital.repository;

import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("seed")
public class DataLoader {

    @Bean
    CommandLineRunner init(UserRepository userRepo, AccountRepository accRepo) {
        return args -> {

            Cliente cliente = new Cliente();
            cliente.setNombre("Bryan Molina");
            cliente.setDocumento("123456789");
            cliente.setEmail("bryan@example.com");
            cliente.setTelefono("3000000000");

            cliente = userRepo.save(cliente);

            Cuenta cuenta = new Cuenta();
            cuenta.setNumeroCuenta("1234567890");
            cuenta.setSaldo(new BigDecimal("150000.00"));
            cuenta.setTipo(TipoCuenta.AHORROS);
            cuenta.setEstado(EstadoCuenta.ACTIVA);
            cuenta.setCliente(cliente);

            accRepo.save(cuenta);
        };
    }
}
